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
package de.intarsys.security.smartcard.smartcardio;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardNotPresentException;
import javax.smartcardio.CardTerminal;

import de.intarsys.security.smartcard.card.CardEvent;
import de.intarsys.security.smartcard.card.ICard;
import de.intarsys.security.smartcard.card.ICardConnection;
import de.intarsys.security.smartcard.card.ICardTerminal;
import de.intarsys.tools.event.Event;
import de.intarsys.tools.event.INotificationListener;

/**
 * javax.smartcardio internal provider implementation.
 * 
 */
public class CardTerminalImpl extends CardTerminal {

	class Listener implements INotificationListener {

		public CountDownLatch latch = new CountDownLatch(1);

		public boolean requirePresent;

		public Listener(boolean waitPresent) {
			super();
			this.requirePresent = waitPresent;
		}

		@Override
		public void handleEvent(Event event) {
			CardEvent ce = (CardEvent) event;
			synchronized (lockWait) {
				if (!ce.getNewState().isInvalid() == requirePresent) {
					if (Log.isLoggable(Level.FINER)) {
						Log.log(Level.FINER, CardTerminalImpl.this.getLabel()
								+ " found state change");
					}
					latch.countDown();
				}
			}
		}
	}

	private static final Logger Log = PACKAGE.Log;

	final private ICardTerminal cardTerminal;

	final private Object lockWait = new Object();

	protected CardTerminalImpl(ICardTerminal cardTerminal) {
		super();
		this.cardTerminal = cardTerminal;
	}

	@Override
	public Card connect(String protocolName) throws CardException {
		// protocol is one of "T=0", "T=1", "T=CL", "*"
		int protocol;
		if ("T=0".equals(protocolName)) {
			protocol = ICardTerminal.PROTOCOL_T0;
		} else if ("T=1".equals(protocolName)) {
			protocol = ICardTerminal.PROTOCOL_T1;
		} else if ("*".equals(protocolName)) {
			protocol = ICardTerminal.PROTOCOL_Tx;
		} else {
			throw new CardException("protocol " + protocolName
					+ " not supported");
		}
		ICard card = cardTerminal.getCard();
		if (card == null || card.getState().isInvalid()) {
			throw new CardNotPresentException("No card present");
		}
		Future<ICardConnection> f = card.connectShared(protocol, null);
		try {
			return createCard(f.get());
		} catch (InterruptedException e) {
			throw new CardException(e);
		} catch (ExecutionException e) {
			throw new CardException(e.getCause());
		}
	}

	protected Card createCard(ICardConnection connection) {
		return new CardImpl(connection);
	}

	public ICardTerminal getCardTerminal() {
		return cardTerminal;
	}

	public String getLabel() {
		return cardTerminal.toString();
	}

	@Override
	public String getName() {
		return cardTerminal.getName();
	}

	@Override
	public boolean isCardPresent() throws CardException {
		ICard card = cardTerminal.getCard();
		return card != null && !card.getState().isInvalid();
	}

	@Override
	public String toString() {
		return getLabel();
	}

	protected boolean waitForCard(long timeout, Listener listener) {
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout must be >= 0" + timeout);
		}
		try {
			if (Log.isLoggable(Level.FINER)) {
				Log.log(Level.FINER, CardTerminalImpl.this.getLabel()
						+ " wait for card");
			}
			synchronized (lockWait) {
				// don't loose signal, synchronize
				cardTerminal.addNotificationListener(CardEvent.ID, listener);
				boolean present = cardTerminal.getCard() != null;
				if (present == listener.requirePresent) {
					return true;
				}
			}
			if (timeout == 0) {
				listener.latch.await();
				return true;
			} else {
				return listener.latch.await(timeout, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
			return false;
		} finally {
			cardTerminal.removeNotificationListener(CardEvent.ID, listener);
		}
	}

	@Override
	public boolean waitForCardAbsent(long timeout) throws CardException {
		return waitForCard(timeout, new Listener(false));
	}

	@Override
	public boolean waitForCardPresent(long timeout) throws CardException {
		return waitForCard(timeout, new Listener(true));
	}

}
