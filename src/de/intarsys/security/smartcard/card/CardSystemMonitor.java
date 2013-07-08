/*
 * Copyright (c) 2013, intarsys consulting GmbH
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of intarsys nor the names of its contributors may be used
 *   to endorse or promote products derived from this software without specific
 *   prior written permission.
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.tools.concurrent.ThreadTools;
import de.intarsys.tools.event.AttributeChangedEvent;
import de.intarsys.tools.event.Event;
import de.intarsys.tools.event.INotificationListener;
import de.intarsys.tools.message.MessageBundle;
import de.intarsys.tools.string.StringTools;

/**
 * Monitors a {@link ICardSystem}'s card terminals and dispatches all events to
 * the four methods:
 * <ul>
 * <li>onCardInserted()</li>
 * <li>onCardRemoved()</li>
 * <li>onCardTerminalConnected()</li>
 * <li>onCardTerminalDisconnected()</li>
 * </ul>
 * Dispatching is using a single, dedicated thread to ensure event ordering.
 */
public class CardSystemMonitor {

	private static int COUNTER = 0;

	private static final Logger Log = PACKAGE.Log;

	private static final MessageBundle Msg = PACKAGE.Messages;

	protected static final ICardTerminal NoCardTerminal = new NoCardTerminal();

	final private List<ICard> cards;

	private final ICardSystem cardSystem;

	final private List<ICardTerminal> cardTerminals;

	private ExecutorService eventExecutor;

	final private int id = COUNTER++;

	private final INotificationListener listenCardEvents = new INotificationListener() {
		@Override
		public void handleEvent(Event event) {
			final CardEvent cardEvent = (CardEvent) event;
			synchronized (lock) {
				// we lock to ensure correct event sequence
				if (getEventExecutor() == null) {
					// defensive
					return;
				}
				getEventExecutor().submit(new Runnable() {
					@Override
					public void run() {
						onCardUpdate(cardEvent.getCard());
					}
				});
			}
		}
	};

	private final INotificationListener listenCardSystemChanged = new INotificationListener() {
		@Override
		public void handleEvent(Event event) {
			final AttributeChangedEvent ace = (AttributeChangedEvent) event;
			synchronized (lock) {
				// we lock to ensure correct event sequence
				if (getEventExecutor() == null) {
					// defensive
					return;
				}
				getEventExecutor().submit(new Runnable() {
					@Override
					public void run() {
						Object value = ace.getOldValue();
						if (value instanceof ICardTerminal) {
							onCardTerminalDisconnect((ICardTerminal) value);
						}
						value = ace.getNewValue();
						if (value instanceof ICardTerminal) {
							onCardTerminalConnect((ICardTerminal) value);
						}
					}
				});
			}
		}
	};

	protected final Object lock = new Object();

	private boolean started = false;

	public CardSystemMonitor(ICardSystem cardSystem) {
		this.cardSystem = cardSystem;
		cardTerminals = new ArrayList<ICardTerminal>();
		cards = new ArrayList<ICard>();
	}

	protected String getCardConnectedMessage() {
		return Msg.getString("CardSystemMonitor.stateConnected"); //$NON-NLS-1$
	}

	protected String getCardConnectingMessage() {
		return Msg.getString("CardSystemMonitor.stateConnecting"); //$NON-NLS-1$
	}

	protected String getCardConnectionFailedMessage(CardException cardException) {
		String msg = cardException.getLocalizedMessage();
		if (msg == null || "null".equals(msg)) { //$NON-NLS-1$
			return Msg
					.getString("CardSystemMonitor.stateConnectedFailedUnspecified"); //$NON-NLS-1$
		} else {
			return Msg.getString("CardSystemMonitor.stateConnectedFailed", //$NON-NLS-1$
					msg);
		}
	}

	protected List<ICard> getCards() {
		synchronized (lock) {
			return new ArrayList<ICard>(cards);
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

	protected List<ICardTerminal> getCardTerminals() {
		synchronized (lock) {
			return new ArrayList<ICardTerminal>(cardTerminals);
		}
	}

	protected String getCardUnsupportedMessage() {
		return Msg.getString("CardSystemMonitor.unsupportedCard"); //$NON-NLS-1$
	}

	protected ExecutorService getEventExecutor() {
		return eventExecutor;
	}

	public boolean isCardAvailable() {
		synchronized (lock) {
			return !cards.isEmpty();
		}
	}

	public boolean isCardTerminalAvailable() {
		synchronized (lock) {
			return cardTerminals.size() > 0
					&& !cardTerminals.contains(NoCardTerminal);
		}
	}

	public boolean isStarted() {
		synchronized (lock) {
			return started;
		}
	}

	protected void onCardChanged(ICard card) {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " " + card + " changed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	protected void onCardInserted(ICard card) {
		Log.info("" + this + " " + card + " inserted"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected void onCardRemoved(ICard card) {
		Log.info("" + this + " " + card + " removed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected void onCardTerminalConnect(ICardTerminal cardTerminal) {
		synchronized (lock) {
			if (!started) {
				return;
			}
			if (cardTerminals.contains(cardTerminal)) {
				if (Log.isLoggable(Level.FINE)) {
					Log.fine("" + this + " " + cardTerminal + " not registered"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				return;
			}
			if (cardTerminal == NoCardTerminal) {
				if (cards.size() > 0) {
					// filter out of sequence event
					return;
				}
			}
			cardTerminals.remove(NoCardTerminal);
			cardTerminals.add(cardTerminal);
		}
		onCardTerminalConnected(cardTerminal);
	}

	protected void onCardTerminalConnected(ICardTerminal terminal) {
		Log.info("" + this + " " + terminal + " connected"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		terminal.addNotificationListener(CardEvent.ID, listenCardEvents);
		onCardUpdate(terminal.getCard());
	}

	protected void onCardTerminalDisconnect(ICardTerminal cardTerminal) {
		synchronized (lock) {
			if (!started) {
				return;
			}
			if (!cardTerminals.contains(cardTerminal)) {
				if (Log.isLoggable(Level.FINE)) {
					Log.fine("" + this + " " + cardTerminal + " not registered"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				return;
			}
			cardTerminals.remove(cardTerminal);
			if (cardTerminals.isEmpty()) {
				cardTerminals.add(NoCardTerminal);
			}
		}
		onCardTerminalDisconnected(cardTerminal);
	}

	protected void onCardTerminalDisconnected(final ICardTerminal terminal) {
		Log.info("" + this + " " + terminal + " disconnected"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		terminal.removeNotificationListener(CardEvent.ID, listenCardEvents);
		onCardUpdate(terminal.getCard());
	}

	protected void onCardUpdate(ICard card) {
		if (card == null) {
			//
		} else if (card.getState().isInvalid()) {
			synchronized (lock) {
				if (!started) {
					return;
				}
				if (!cards.contains(card)) {
					if (Log.isLoggable(Level.FINE)) {
						Log.fine("" + this + " " + card + " not registered"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
					return;
				}
				cards.remove(card);
			}
			onCardRemoved(card);
		} else {
			boolean change = false;
			synchronized (lock) {
				if (!started) {
					return;
				}
				if (cards.contains(card)) {
					change = true;
				} else {
					cards.add(card);
				}
			}
			if (change) {
				onCardChanged(card);
			} else {
				onCardInserted(card);
			}
		}
	}

	public void start() {
		synchronized (lock) {
			if (started) {
				return;
			}
			started = true;
			eventExecutor = Executors
					.newSingleThreadExecutor(ThreadTools
							.newThreadFactoryDaemon("CardSystemMonitor Executor Thread")); //$NON-NLS-1$
			Log.info("" + this + " start"); //$NON-NLS-1$ //$NON-NLS-2$
			cardTerminals.clear();
			cards.clear();
			cardSystem.addNotificationListener(AttributeChangedEvent.ID,
					listenCardSystemChanged);
			getEventExecutor().submit(new Runnable() {
				@Override
				public void run() {
					onCardTerminalConnect(NoCardTerminal);
				}
			});
			ICardTerminal[] tempTerminals = cardSystem.getCardTerminals();
			for (final ICardTerminal cardTerminal : tempTerminals) {
				getEventExecutor().submit(new Runnable() {
					@Override
					public void run() {
						onCardTerminalConnect(cardTerminal);
					}
				});
			}
		}
	}

	public void stop() {
		List<ICardTerminal> tempTerminals;
		synchronized (lock) {
			if (!started) {
				return;
			}
			started = false;
			Log.info("" + this + " stop"); //$NON-NLS-1$ //$NON-NLS-2$
			cardSystem.removeNotificationListener(AttributeChangedEvent.ID,
					listenCardSystemChanged);
			tempTerminals = new ArrayList<ICardTerminal>(cardTerminals);
			for (ICardTerminal cardTerminal : tempTerminals) {
				cardTerminal.removeNotificationListener(CardEvent.ID,
						listenCardEvents);
			}
			if (eventExecutor != null) {
				eventExecutor.shutdownNow();
			}
			eventExecutor = null;
			cardTerminals.clear();
			cards.clear();
		}
	}

	@Override
	public String toString() {
		return getClass().getName() + " " + id; //$NON-NLS-1$
	}
}
