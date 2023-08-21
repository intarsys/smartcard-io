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

import java.util.Arrays;
import java.util.List;

import de.intarsys.tools.concurrent.TaskFailed;
import de.intarsys.tools.event.AttributeChangedEvent;
import de.intarsys.tools.event.Event;
import de.intarsys.tools.event.EventDispatcher;
import de.intarsys.tools.event.EventType;
import de.intarsys.tools.event.INotificationListener;
import de.intarsys.tools.exception.ExceptionTools;
import de.intarsys.tools.message.IMessageBundle;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * A concrete monitor that manages a single active connection.
 * 
 */
public class ActiveCardConnectionMonitor extends CommonCardConnectionMonitor {

	public static class State {

		private final List<ICardTerminal> cardTerminals;

		private final String activeCardStateMessage;

		private final ICardTerminal activeCardTerminal;

		private final ICardConnection activeCardConnection;

		public State(List<ICardTerminal> cardTerminals, ICardTerminal activeCardTerminal, ICardConnection connection,
				String cardStateMessage) {
			this.cardTerminals = cardTerminals;
			this.activeCardConnection = connection;
			this.activeCardStateMessage = cardStateMessage;
			this.activeCardTerminal = activeCardTerminal;
		}

		public ICardConnection getActiveCardConnection() {
			return activeCardConnection;
		}

		public String getActiveCardStateMessage() {
			return activeCardStateMessage;
		}

		public ICardTerminal getActiveCardTerminal() {
			return activeCardTerminal;
		}

		public List<ICardTerminal> getCardTerminals() {
			return cardTerminals;
		}

	}

	private static ILogger Log = PACKAGE.Log;

	private static final IMessageBundle Msg = PACKAGE.Messages;

	private final EventDispatcher dispatcher = new EventDispatcher(this);

	private ICardTerminal activeCardTerminal;

	private String activeCardStateMessage;

	private ICardConnection activeCardConnection;

	public ActiveCardConnectionMonitor(ICardSystem cardSystem) {
		super(cardSystem);
	}

	/**
	 * call from synchronized code only
	 * 
	 * @return
	 */
	protected void acceptConnection(ICardConnection connection) throws Exception {
		activeCardTerminal = connection.getCardTerminal();
		activeCardConnection = connection;
		activeCardStateMessage = getCardConnectedMessage();
	}

	public void addNotificationListener(EventType type, INotificationListener listener) {
		dispatcher.addNotificationListener(type, listener);
	}

	/**
	 * Call from within synchronized code only.
	 * 
	 * @return The current monitor state.
	 */
	protected State createState() {
		return new State(Arrays.asList(getCardSystem().getCardTerminals()), activeCardTerminal, activeCardConnection,
				activeCardStateMessage);
	}

	protected ICardConnection getActiveCardConnection() {
		return activeCardConnection;
	}

	protected String getActiveCardStateMessage() {
		return activeCardStateMessage;
	}

	protected ICardTerminal getActiveCardTerminal() {
		return activeCardTerminal;
	}

	public ActiveCardConnectionMonitor.State getState() {
		synchronized (lock) {
			return createState();
		}
	}

	@Override
	protected void onCardInserted(ICard card) {
		super.onCardInserted(card);
		State state;
		synchronized (lock) {
			if (activeCardConnection != null) {
				return;
			}
			// auto select first card presented
			select(card.getCardTerminal());
			state = createState();
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null, state));
	}

	@Override
	protected void onCardRemoved(ICard card) {
		super.onCardRemoved(card);
		State state;
		synchronized (lock) {
			if (activeCardConnection == null || card != activeCardConnection.getCard()) {
				return;
			}
			// deselect if currently active
			select(card.getCardTerminal());
			state = createState();
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null, state));
	}

	@Override
	protected void onCardTerminalConnected(ICardTerminal terminal) {
		super.onCardTerminalConnected(terminal);
		State state;
		synchronized (lock) {
			if (activeCardTerminal == null) {
				// auto select first terminal presented
				select(terminal);
			}
			state = createState();
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null, state));
	}

	@Override
	protected void onCardTerminalDisconnected(final ICardTerminal terminal) {
		super.onCardTerminalDisconnected(terminal);
		State state;
		synchronized (lock) {
			if (activeCardTerminal == terminal) {
				// auto select first terminal available
				ICardTerminal[] terminals = getCardSystem().getCardTerminals();
				if (terminals.length == 0) {
					select(null);
				} else {
					select(terminals[0]);
				}
			}
			state = createState();
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null, state));
	}

	@Override
	protected void onConnected(ICardConnection connection) throws CardException {
		State state;
		synchronized (lock) {
			if (activeCardConnection != null) {
				Log.trace("{} ignore {}", getLogPrefix(), connection); //$NON-NLS-1$
				try {
					// ignore connection
					connection.close(ICardConnection.MODE_LEAVE_CARD);
				} catch (CardException e) {
					//
				}
				return;
			}
			Log.trace("{} {} established", getLogPrefix(), connection); //$NON-NLS-1$
			try {
				acceptConnection(connection);
			} catch (Exception e) {
				CardException nested = ExceptionTools.getFromChain(e, CardException.class);
				if (nested != null) {
					throw nested;
				}
				throw CardException.create(e);
			}
			state = createState();
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null, state));
	}

	@Override
	protected void onConnectedGiveup(ICardConnection connection) {
		super.onConnectedGiveup(connection);
		State state;
		synchronized (lock) {
			activeCardStateMessage = getCardUnsupportedMessage();
			state = createState();
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null, state));
	}

	@Override
	protected void onConnectionFailed(ICard card, TaskFailed exception) {
		State state;
		synchronized (lock) {
			if (activeCardConnection != null) {
				Log.trace("{} {} connect failure ignored", getLogPrefix(), card); //$NON-NLS-1$
				return;
			}
			activeCardStateMessage = getCardConnectionFailedMessage(exception);
			state = createState();
		}
		Log.debug("{} {} connect failed", getLogPrefix(), card, exception); //$NON-NLS-1$
		triggerEvent(new AttributeChangedEvent(this, "state", null, state));
	}

	public void removeNotificationListener(EventType type, INotificationListener listener) {
		dispatcher.removeNotificationListener(type, listener);
	}

	/**
	 * call from synchronized code only
	 * 
	 * @return
	 */
	protected void resetConnection() {
		if (activeCardConnection != null) {
			try {
				activeCardConnection.close(ICardConnection.MODE_LEAVE_CARD);
			} catch (CardException e) {
				Log.warn("{} {} disconnect failed", getLogPrefix(), activeCardConnection); //$NON-NLS-1$
			}
		}
		activeCardConnection = null;
	}

	/**
	 * This may be called in synchronized section only
	 * 
	 * @param cardTerminal
	 */
	protected void select(ICardTerminal cardTerminal) {
		activeCardTerminal = cardTerminal;
		ICard card = cardTerminal == null ? null : cardTerminal.getCard();
		Log.trace("{} select {}, {}", getLogPrefix(), cardTerminal, card); //$NON-NLS-1$
		activeCardStateMessage = getCardStateMessage(card);
		resetConnection();
		if (card != null && isStarted()) {
			EnumCardState state = card.getState();
			if (state.isConnectedShared() || state.isNotConnected()) {
				activeCardStateMessage = getCardConnectingMessage();
				connect(card);
			}
		}
	}

	protected void setActiveCardConnection(ICardConnection activeCardConnection) {
		this.activeCardConnection = activeCardConnection;
	}

	protected void setActiveCardStateMessage(String activeCardStateMessage) {
		this.activeCardStateMessage = activeCardStateMessage;
	}

	public void setActiveCardTerminal(ICardTerminal newActiveTerminal) {
		State state;
		synchronized (lock) {
			if (newActiveTerminal != null && !newActiveTerminal.isDisposed()
					&& activeCardTerminal != newActiveTerminal) {
				select(newActiveTerminal);
			} else {
				return;
			}
			state = createState();
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null, state));
	}

	@Override
	public void stop() {
		super.stop();
		synchronized (lock) {
			select(null);
		}
	}

	protected void triggerEvent(Event event) {
		if (!isStarted()) {
			return;
		}
		dispatcher.triggerEvent(event);
	}
}
