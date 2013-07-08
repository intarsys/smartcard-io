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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.tools.event.AttributeChangedEvent;
import de.intarsys.tools.event.Event;
import de.intarsys.tools.event.EventDispatcher;
import de.intarsys.tools.event.EventType;
import de.intarsys.tools.event.INotificationListener;
import de.intarsys.tools.message.MessageBundle;

/**
 * A {@link CardSystemMonitor} that connects to an {@link ICard} card detection.
 * 
 */
public class ActiveCardConnectionMonitor extends CardConnectionMonitor {

	public static class State {

		private final List<ICardTerminal> cardTerminals;

		private final String activeCardStateMessage;

		private final ICardTerminal activeCardTerminal;

		private final ICardConnection activeCardConnection;

		public State(List<ICardTerminal> cardTerminals,
				ICardTerminal activeCardTerminal, ICardConnection connection,
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

	final private EventDispatcher dispatcher = new EventDispatcher(this);

	private static Logger Log = PACKAGE.Log;

	private ICardTerminal activeCardTerminal;

	private String activeCardStateMessage;

	private ICardConnection activeCardConnection;

	private static final MessageBundle Msg = PACKAGE.Messages;

	public ActiveCardConnectionMonitor(ICardSystem cardSystem) {
		super(cardSystem);
	}

	public void addNotificationListener(EventType type,
			INotificationListener listener) {
		dispatcher.addNotificationListener(type, listener);
	}

	protected State createState() {
		return new State(getCardTerminals(), activeCardTerminal,
				activeCardConnection, activeCardStateMessage);
	}

	public State getState() {
		synchronized (lock) {
			return createState();
		}
	}

	@Override
	protected void onCardInserted(ICard card) {
		super.onCardInserted(card);
		synchronized (lock) {
			if (isConnectTaskRunning(card) || activeCardConnection != null) {
				return;
			}
			// auto select first card presented
			select(card.getCardTerminal());
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null,
				createState()));
	}

	@Override
	protected void onCardRemoved(ICard card) {
		super.onCardRemoved(card);
		synchronized (lock) {
			if (activeCardConnection == null
					|| card != activeCardConnection.getCard()) {
				return;
			}
			// deselect if currently active
			select(card.getCardTerminal());
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null,
				createState()));
	}

	@Override
	protected void onCardTerminalConnected(ICardTerminal terminal) {
		super.onCardTerminalConnected(terminal);
		synchronized (lock) {
			if (activeCardTerminal == null
					|| activeCardTerminal == NoCardTerminal) {
				// auto select first terminal presented
				select(terminal);
			}
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null,
				createState()));
	}

	@Override
	protected void onCardTerminalDisconnected(final ICardTerminal terminal) {
		super.onCardTerminalDisconnected(terminal);
		synchronized (lock) {
			if (activeCardTerminal == terminal) {
				// auto select first terminal available
				ICardTerminal newTerminal = getCardTerminals().get(0);
				select(newTerminal);
			}
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null,
				createState()));
	}

	@Override
	protected void onConnected(ICardConnection connection, Object data) {
		synchronized (lock) {
			if (activeCardConnection != null) {
				if (Log.isLoggable(Level.FINEST)) {
					Log.finest("" + this + " disconnect; active connection " + activeCardConnection + "; active terminal " + activeCardTerminal + "; new card " + connection.getCard()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				try {
					connection.close(ICardConnection.MODE_LEAVE_CARD);
				} catch (CardException e) {
					Log.warning("" + this + " disconnect failed; active connection " + activeCardConnection + "; active terminal " + activeCardTerminal + "; new card " + connection.getCard()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				return;
			}

			activeCardConnection = connection;
			if (Log.isLoggable(Level.FINEST)) {
				Log.finest("" + this + " connection " + connection + " established"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			activeCardStateMessage = getCardConnectedMessage();
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null,
				createState()));
	}

	@Override
	protected void onConnectionFailed(ICard card, Object data,
			CardException cardException) {
		synchronized (lock) {
			activeCardStateMessage = getCardConnectionFailedMessage(cardException);
		}
		if (Log.isLoggable(Level.FINE)) {
			Log.log(Level.FINE, "" + this + " connection to " + card //$NON-NLS-1$ //$NON-NLS-2$
					+ " failed", cardException); //$NON-NLS-1$
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null,
				createState()));
	}

	public void removeNotificationListener(EventType type,
			INotificationListener listener) {
		dispatcher.removeNotificationListener(type, listener);
	}

	/**
	 * This may be called in synchronized section only
	 * 
	 * @param cardTerminal
	 */
	protected void select(ICardTerminal cardTerminal) {
		activeCardTerminal = cardTerminal;
		ICard card = cardTerminal == null ? null : cardTerminal.getCard();
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " select terminal " + cardTerminal //$NON-NLS-1$ //$NON-NLS-2$
					+ ", card " + card); //$NON-NLS-1$
		}
		activeCardStateMessage = getCardStateMessage(card);
		if (activeCardConnection != null) {
			try {
				activeCardConnection.close(ICardConnection.MODE_LEAVE_CARD);
			} catch (CardException e) {
				Log.warning("" + this + " disconnect failed; active connection " + activeCardConnection + "; active terminal " + activeCardTerminal); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			}
		}
		activeCardConnection = null;
		if (card != null && isStarted()) {
			EnumCardState state = card.getState();
			if (state.isConnectedShared() || state.isNotConnected()) {
				activeCardStateMessage = getCardConnectingMessage();
				connect(card, null);
			}
		}
	}

	public void setActiveCardTerminal(ICardTerminal newActiveTerminal) {
		synchronized (lock) {
			if (getCardTerminals().contains(newActiveTerminal)
					&& activeCardTerminal != newActiveTerminal) {
				select(newActiveTerminal);
			} else {
				return;
			}
		}
		triggerEvent(new AttributeChangedEvent(this, "state", null,
				createState()));
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
