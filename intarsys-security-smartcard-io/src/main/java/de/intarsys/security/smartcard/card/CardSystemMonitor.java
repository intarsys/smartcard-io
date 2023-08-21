/*
 * Copyright (c) 2013, intarsys GmbH
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - Neither the name of intarsys nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific
 * prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package de.intarsys.security.smartcard.card;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import de.intarsys.tools.attribute.Attribute;
import de.intarsys.tools.concurrent.TaskFailed;
import de.intarsys.tools.concurrent.ThreadTools;
import de.intarsys.tools.event.AttributeChangedEvent;
import de.intarsys.tools.event.INotificationListener;
import de.intarsys.tools.message.IMessageBundle;
import de.intarsys.tools.reflect.ObjectTools;
import de.intarsys.tools.string.StringTools;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * Monitors a {@link ICardSystem}'s {@link ICardTerminal} and {@link ICard}
 * instances and dispatches all events to an {@link ICardSystemListener}.
 * 
 * {@link CardSystemMonitor} is using a dedicated executor for event
 * dispatching.
 */
public class CardSystemMonitor {

	/**
	 * A callback interface for events generated by the
	 * {@link CardSystemMonitor}.
	 * 
	 */
	public interface ICardSystemListener {

		/**
		 * The {@link ICard#getState()} has changed.
		 * 
		 * @param card
		 */
		public void onCardChanged(ICard card);

		/**
		 * A new {@link ICard} is available in an {@link ICardTerminal}.
		 * 
		 * @param card
		 */
		public void onCardInserted(ICard card);

		/**
		 * An {@link ICard} is removed from an {@link ICardTerminal}.
		 * 
		 * @param card
		 */
		public void onCardRemoved(ICard card);

		/**
		 * A new {@link ICardTerminal} is connected to the system.
		 * 
		 * @param terminal
		 */
		public void onCardTerminalConnected(ICardTerminal terminal);

		/**
		 * An {@link ICardTerminal} is disconnected from the system.
		 * 
		 * @param terminal
		 */
		public void onCardTerminalDisconnected(ICardTerminal terminal);

	}

	private static final ILogger Log = PACKAGE.Log;

	private static final IMessageBundle Msg = PACKAGE.Messages;

	private final ScheduledExecutorService eventExecutor;

	private final ICardSystem cardSystem;

	private final String label;

	private final List<ICardSystemListener> listeners = new CopyOnWriteArrayList<>();

	private final INotificationListener<CardEvent> listenCardEvents = new INotificationListener<CardEvent>() {
		@Override
		public void handleEvent(final CardEvent event) {
			if (!isStarted()) {
				return;
			}
			getEventExecutor().submit(new Runnable() {
				@Override
				public void run() {
					if (!isStarted()) {
						return;
					}
					onCardUpdate(event.getCard(), event.getNewState());
				}
			});
		}
	};

	private final INotificationListener<AttributeChangedEvent> listenCardSystemChanged = new INotificationListener<AttributeChangedEvent>() {
		@Override
		public void handleEvent(final AttributeChangedEvent event) {
			// requesting the lock is crucial for correct initialization
			// sequence!!
			if (!isStarted()) {
				return;
			}
			getEventExecutor().submit(new Runnable() {
				@Override
				public void run() {
					if (!isStarted()) {
						return;
					}
					Object value = event.getOldValue();
					if (value instanceof ICardTerminal) {
						onCardTerminalDisconnect((ICardTerminal) value);
					}
					value = event.getNewValue();
					if (value instanceof ICardTerminal) {
						onCardTerminalConnect((ICardTerminal) value);
					}
				}
			});
		}
	};

	protected final Object lock = new Object();

	private boolean started = false;

	private final Attribute attrSeen = new Attribute("seen"); //$NON-NLS-1$

	public CardSystemMonitor(ICardSystem cardSystem) {
		this.label = ObjectTools.createLabel(this);
		this.cardSystem = cardSystem;
		this.eventExecutor = Executors.newSingleThreadScheduledExecutor(ThreadTools.newThreadFactoryDaemon(toString()));
	}

	public CardSystemMonitor(ICardSystem cardSystem, ScheduledExecutorService executor) {
		this.label = ObjectTools.createLabel(this);
		this.cardSystem = cardSystem;
		this.eventExecutor = executor;
	}

	public void addCardSystemListener(ICardSystemListener listener) {
		listeners.add(listener);
	}

	protected boolean checkSeen(ICard card) {
		Boolean seen = getSeen(card);
		if (seen == null || !seen) {
			setSeen(card, true);
			return false;
		}
		return true;
	}

	public void dispose() {
		stop();
		getEventExecutor().shutdownNow();
	}

	protected String getCardConnectedMessage() {
		return Msg.getString("CardSystemMonitor.stateConnected"); //$NON-NLS-1$
	}

	protected String getCardConnectingMessage() {
		return Msg.getString("CardSystemMonitor.stateConnecting"); //$NON-NLS-1$
	}

	protected String getCardConnectionFailedMessage(TaskFailed exception) {
		if (exception.isCancellation()) {
			return Msg.getString("CardSystemMonitor.stateConnectedFailedCancel"); //$NON-NLS-1$
		} else {
			String msg = exception.getCause().getLocalizedMessage();
			if (msg == null || "null".equals(msg)) { //$NON-NLS-1$
				return Msg.getString("CardSystemMonitor.stateConnectedFailedUnspecified"); //$NON-NLS-1$
			} else {
				return Msg.getString("CardSystemMonitor.stateConnectedFailed", //$NON-NLS-1$
						msg);
			}
		}
	}

	protected String getCardStateMessage(ICard card) {
		if (card == null) {
			return Msg.getString("CardSystemMonitor.stateNoCardAvailable"); //$NON-NLS-1$
		}
		EnumCardState state = card.getState();
		if (state.isInvalid()) {
			return Msg.getString("CardSystemMonitor.stateNoCardAvailable"); //$NON-NLS-1$
		} else if (state.isConnectedExclusive()) {
			return Msg.getString("CardSystemMonitor.stateConnectedExclusive"); //$NON-NLS-1$
		} else if (state.isConnectedShared() || state.isNotConnected()) {
			return Msg.getString("CardSystemMonitor.stateNotConnected"); //$NON-NLS-1$
		}
		return StringTools.EMPTY;
	}

	public ICardSystem getCardSystem() {
		return cardSystem;
	}

	protected String getCardUnsupportedMessage() {
		return Msg.getString("CardSystemMonitor.unsupportedCard"); //$NON-NLS-1$
	}

	protected ScheduledExecutorService getEventExecutor() {
		return eventExecutor;
	}

	protected Object getLogPrefix() {
		return label;
	}

	protected Boolean getSeen(ICard card) {
		return (Boolean) card.getAttribute(attrSeen);
	}

	public boolean isCardAvailable() {
		for (ICardTerminal terminal : getCardSystem().getCardTerminals()) {
			if (terminal.getCard() != null) {
				return true;
			}
		}
		return false;
	}

	public boolean isCardTerminalAvailable() {
		return getCardSystem().getCardTerminals().length > 0;
	}

	public boolean isStarted() {
		synchronized (lock) {
			return started;
		}
	}

	protected void onCardChanged(ICard card) {
		for (ICardSystemListener listener : listeners) {
			listener.onCardChanged(card);
		}
	}

	protected void onCardInserted(ICard card) {
		Log.debug("{} {} inserted", getLogPrefix(), card); //$NON-NLS-1$
		for (ICardSystemListener listener : listeners) {
			listener.onCardInserted(card);
		}
	}

	protected void onCardRemoved(ICard card) {
		Log.debug("{} {} removed", getLogPrefix(), card); //$NON-NLS-1$
		for (ICardSystemListener listener : listeners) {
			listener.onCardRemoved(card);
		}
	}

	protected void onCardTerminalConnect(ICardTerminal terminal) {
		if (terminal.isDisposed()) {
			return;
		}
		Log.info("{} {} connected", getLogPrefix(), terminal); //$NON-NLS-1$
		for (ICardSystemListener listener : listeners) {
			listener.onCardTerminalConnected(terminal);
		}
		onCardTerminalConnected(terminal);
		terminal.addNotificationListener(CardEvent.ID, listenCardEvents);
		ICard card = terminal.getCard();
		if (card != null) {
			onCardUpdate(card, card.getState());
		}
	}

	/**
	 * This method is always running in the executor context. This is the reason
	 * why it is not necessary to synchronize when adding the listener and call
	 * "onCardUpdate" - external card updates are always enqueued.
	 * 
	 * @param terminal
	 */
	protected void onCardTerminalConnected(ICardTerminal terminal) {
		//
	}

	protected void onCardTerminalDisconnect(ICardTerminal terminal) {
		Log.info("{} {} disconnected", getLogPrefix(), terminal); //$NON-NLS-1$
		for (ICardSystemListener listener : listeners) {
			listener.onCardTerminalDisconnected(terminal);
		}
		onCardTerminalDisconnected(terminal);
		terminal.removeNotificationListener(CardEvent.ID, listenCardEvents);
	}

	/**
	 * This method is always running in the executor context. This is the reason
	 * why it is not necessary to synchronize when adding the listener and call
	 * "onCardUpdate" - external card updates are always enqueued.
	 * 
	 * When "disconnected" is called back, the terminal is already disposed, the
	 * respective card state change callback is already performed.
	 * 
	 * @param terminal
	 */
	protected void onCardTerminalDisconnected(final ICardTerminal terminal) {
		//
	}

	protected void onCardUpdate(ICard card, EnumCardState state) {
		if (state.isInvalid()) {
			onCardRemoved(card);
		} else {
			if (checkSeen(card)) {
				onCardChanged(card);
			} else {
				onCardInserted(card);
			}
		}
	}

	protected void onStarted() {

		//
	}

	public void removeCardSystemListener(ICardSystemListener listener) {
		listeners.remove(listener);
	}

	protected void setSeen(ICard card, boolean value) {
		card.setAttribute(attrSeen, value);
	}

	public void start() {
		synchronized (lock) {
			if (started) {
				return;
			}
			/*
			 * initial terminal request is *before* we are started and listener is attached, may get
			 * "terminal connected" events otherwise.
			 */
			ICardTerminal[] tempTerminals = cardSystem.getCardTerminals();
			started = true;
			Log.info("{} start", getLogPrefix()); //$NON-NLS-1$
			cardSystem.addNotificationListener(AttributeChangedEvent.ID, listenCardSystemChanged);
			for (final ICardTerminal cardTerminal : tempTerminals) {
				getEventExecutor().submit(new Runnable() {
					@Override
					public void run() {
						if (!isStarted()) {
							return;
						}
						onCardTerminalConnect(cardTerminal);
					}
				});
			}
			getEventExecutor().submit(new Runnable() {
				@Override
				public void run() {
					onStarted();
				}
			});
		}
	}

	public void stop() {
		synchronized (lock) {
			if (!started) {
				return;
			}
			started = false;
			Log.info("{} stop", getLogPrefix()); //$NON-NLS-1$
			cardSystem.removeNotificationListener(AttributeChangedEvent.ID, listenCardSystemChanged);
			for (ICardTerminal cardTerminal : getCardSystem().getCardTerminals()) {
				cardTerminal.removeNotificationListener(CardEvent.ID, listenCardEvents);
				// unmark cards to support restart
				ICard card = cardTerminal.getCard();
				if (card != null) {
					setSeen(card, false);
				}
			}
		}
	}

	@Override
	public String toString() {
		return label;
	}
}
