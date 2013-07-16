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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;

import de.intarsys.security.smartcard.card.CardSystemMonitor;
import de.intarsys.security.smartcard.card.ICard;
import de.intarsys.security.smartcard.card.ICardSystem;
import de.intarsys.security.smartcard.card.ICardTerminal;
import de.intarsys.security.smartcard.card.standard.StandardCardSystem;
import de.intarsys.security.smartcard.pcsc.IPCSCContextFactory;
import de.intarsys.tools.attribute.Attribute;

/**
 * javax.smartcardio internal provider implementation.
 * 
 */
public class CardTerminalsImpl extends CardTerminals {

	class Listener implements CardSystemMonitor.ICardSystemListener {

		public CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void onCardChanged(ICard card) {
			// this is of no particular interest here...
		}

		@Override
		public void onCardInserted(ICard card) {
			synchronized (lockWait) {
				if (checkStateChange(card.getCardTerminal())) {
					if (Log.isLoggable(Level.FINER)) {
						Log.log(Level.FINER, CardTerminalsImpl.this.getLabel()
								+ " found state change");
					}
					latch.countDown();
				}
			}
		}

		@Override
		public void onCardRemoved(ICard card) {
			synchronized (lockWait) {
				if (checkStateChange(card.getCardTerminal())) {
					if (Log.isLoggable(Level.FINER)) {
						Log.log(Level.FINER, CardTerminalsImpl.this.getLabel()
								+ " found state change");
					}
					latch.countDown();
				}
			}
		}

		@Override
		public void onCardTerminalConnected(ICardTerminal terminal) {
			// this is of no particular interest here...
		}

		@Override
		public void onCardTerminalDisconnected(ICardTerminal terminal) {
			// this is of no particular interest here...
		}
	}

	static class ReaderState {

		final public boolean present;

		final public boolean change;

		public ReaderState(boolean present, boolean change) {
			super();
			this.present = present;
			this.change = change;
		}
	}

	private static final Logger Log = PACKAGE.Log;

	final private Object lockWait = new Object();

	final private ICardSystem cardSystem;

	final private Attribute attrCardTerminalImpl = new Attribute(
			"cardTerminalImpl");

	final private Attribute attrState = new Attribute("state");

	protected CardTerminalsImpl(IPCSCContextFactory contextFactory) {
		super();
		this.cardSystem = new StandardCardSystem(contextFactory);
	}

	protected boolean checkStateChange(ICardTerminal terminal) {
		ReaderState freezeState = getState(terminal);
		boolean newPresent = terminal.getCard() != null;
		if (freezeState != null) {
			boolean oldPresent = freezeState.present;
			if (newPresent != oldPresent) {
				setState(terminal, new ReaderState(newPresent, true));
				return true;
			} else {
				return false;
			}
		} else {
			setState(terminal, new ReaderState(newPresent, true));
			return true;
		}
	}

	/**
	 * Create unique {@link CardTerminalImpl} instances per
	 * {@link ICardTerminal}.
	 * 
	 * @param cardTerminal
	 * @return
	 */
	protected CardTerminal createCardTerminal(ICardTerminal cardTerminal) {
		CardTerminalImpl result = (CardTerminalImpl) cardTerminal
				.getAttribute(attrCardTerminalImpl);
		if (result == null) {
			result = new CardTerminalImpl(cardTerminal);
			cardTerminal.setAttribute(attrCardTerminalImpl, result);
		}
		return result;
	}

	public ICardSystem getCardSystem() {
		return cardSystem;
	}

	protected String getLabel() {
		return "CardTerminals";
	}

	protected ReaderState getState(ICardTerminal terminal) {
		return (ReaderState) terminal.getAttribute(attrState);
	}

	@Override
	public List<CardTerminal> list(State state) throws CardException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.log(Level.FINEST, CardTerminalsImpl.this.getLabel() + " list");
		}
		ICardTerminal[] terminals = cardSystem.getCardTerminals();
		List<CardTerminal> result = new ArrayList<>();
		for (int i = 0; i < terminals.length; i++) {
			ICardTerminal terminal = terminals[i];
			ICard card = terminal.getCard();
			boolean newPresent = card != null;
			if (state == State.ALL) {
				result.add(createCardTerminal(terminal));
			} else if (state == State.CARD_ABSENT) {
				if (!newPresent) {
					result.add(createCardTerminal(terminal));
				}
			} else if (state == State.CARD_PRESENT) {
				if (newPresent) {
					result.add(createCardTerminal(terminal));
				}
			} else if (state == State.CARD_INSERTION) {
				ReaderState freezeState = getState(terminal);
				if (freezeState == null) {
					freezeState = new ReaderState(newPresent, true);
				}
				if (freezeState.change && freezeState.present) {
					result.add(createCardTerminal(terminal));
				}
			} else if (state == State.CARD_REMOVAL) {
				ReaderState freezeState = getState(terminal);
				if (freezeState == null) {
					freezeState = new ReaderState(newPresent, true);
				}
				if (freezeState.change && !freezeState.present) {
					result.add(createCardTerminal(terminal));
				}
			} else {
				throw new CardException("Unknown state: " + state);
			}
		}
		return result;
	}

	protected void setState(ICardTerminal terminal, ReaderState stateChange) {
		terminal.setAttribute(attrState, stateChange);
	}

	@Override
	public boolean waitForChange(long timeout) throws CardException {
		if (timeout < 0) {
			throw new IllegalArgumentException("timeout must be >= 0" + timeout);
		}
		CardSystemMonitor monitor = new CardSystemMonitor(getCardSystem());
		Listener listener = new Listener();
		monitor.addCardSystemListener(listener);
		if (Log.isLoggable(Level.FINER)) {
			Log.log(Level.FINER, CardTerminalsImpl.this.getLabel()
					+ " wait for change " + timeout);
		}
		synchronized (lockWait) {
			// we don't need to check here for change,
			// monitor has replay semantics
			monitor.start();
			ICardTerminal[] terminals = cardSystem.getCardTerminals();
			for (int i = 0; i < terminals.length; i++) {
				ICardTerminal terminal = terminals[i];
				ReaderState freezeState = getState(terminal);
				if (freezeState != null) {
					setState(terminal, new ReaderState(freezeState.present,
							false));
				} else {
					setState(terminal, new ReaderState(
							terminal.getCard() != null, false));
				}
			}
		}
		try {
			if (timeout == 0) {
				listener.latch.await();
				return true;
			} else {
				return listener.latch.await(timeout, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e) {
			return false;
		} finally {
			monitor.stop();
		}
	}
}
