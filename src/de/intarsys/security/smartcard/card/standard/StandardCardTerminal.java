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
package de.intarsys.security.smartcard.card.standard;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.security.smartcard.card.ATR;
import de.intarsys.security.smartcard.card.CardException;
import de.intarsys.security.smartcard.card.CardReset;
import de.intarsys.security.smartcard.card.CardSharingViolationException;
import de.intarsys.security.smartcard.card.CommonCardConnection;
import de.intarsys.security.smartcard.card.CommonCardTerminal;
import de.intarsys.security.smartcard.card.EnumCardState;
import de.intarsys.security.smartcard.pcsc.IPCSCCardReader;
import de.intarsys.security.smartcard.pcsc.IPCSCContext;
import de.intarsys.security.smartcard.pcsc.PCSCCardReaderState;
import de.intarsys.security.smartcard.pcsc.PCSCConnection;
import de.intarsys.security.smartcard.pcsc.PCSCException;
import de.intarsys.security.smartcard.pcsc.PCSCReset;
import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES;
import de.intarsys.tools.event.INotificationSupport;

/**
 * The abstraction of a single terminal, optionally containing a token,
 * connected to the system.
 * <p>
 * The standard implementation maps directly to the native PCSC API. The initial
 * {@link IPCSCContext} is defined by the {@link IPCSCCardReader} instance.
 * 
 * Upon connecting, this implementation will create a new {@link IPCSCContext}
 * AND a {@link PCSCConnection}. This is done for maximum decoupling and PCSC
 * platform and version independence (some platforms may serialize requests to
 * the same context).
 * <p>
 * {@link StandardCardTerminal} is used in a multithreaded environment.
 */
public class StandardCardTerminal extends CommonCardTerminal implements
		INotificationSupport {

	private final static Logger Log = PACKAGE.Log;

	public static EnumCardState mapToEnumCardState(
			PCSCCardReaderState readerState) {
		EnumCardState cardState = null;
		if (readerState.isPresent()) {
			cardState = EnumCardState.NOT_CONNECTED;
			if (readerState.isExclusive()) {
				cardState = EnumCardState.CONNECTED_EXCLUSIVE;
			} else if (readerState.isInUse()) {
				cardState = EnumCardState.CONNECTED_SHARED;
			}
		}
		return cardState;
	}

	final private IPCSCCardReader.IStatusListener listenStatus = new IPCSCCardReader.IStatusListener() {
		@Override
		public void onException(PCSCException e) {
			dispose();
		}

		@Override
		public void onStatusChange(PCSCCardReaderState cardReaderState) {
			updateCardState(cardReaderState);
		}
	};

	final private IPCSCCardReader pcscCardReader;

	protected StandardCardTerminal(StandardCardSystem cardSystem,
			IPCSCCardReader pcscCardReader) throws CardException, PCSCException {
		super(pcscCardReader.getId(), cardSystem);
		this.pcscCardReader = pcscCardReader;
		initialize();
	}

	@Override
	protected CommonCardConnection basicConnectDirect(int id,
			ScheduledExecutorService executor) throws CardException {
		try {
			IPCSCContext context = getPcscCardReader().getContext()
					.establishContext();
			PCSCConnection connection = context.connect(getPcscCardReader()
					.getName(), _IPCSC.SCARD_SHARE_DIRECT,
					_IPCSC.SCARD_PROTOCOL_UNDEFINED);
			StandardCardConnection newChannel = new StandardCardConnection(
					this, id, executor, true, connection);
			return newChannel;
		} catch (PCSCReset e) {
			throw new CardReset();
		} catch (PCSCException e) {
			throw new CardException(e.getLocalizedMessage(), e);
		}
	}

	protected StandardCardConnection basicConnectExclusive(StandardCard card,
			int id, ScheduledExecutorService executor) throws CardException {
		try {
			IPCSCContext context = getPcscCardReader().getContext()
					.establishContext();
			PCSCConnection pcscConnection = context.connect(getPcscCardReader()
					.getName(), _IPCSC.SCARD_SHARE_EXCLUSIVE,
					_IPCSC.SCARD_PROTOCOL_Tx);
			return new StandardCardConnection(card, id, executor, true,
					pcscConnection);
		} catch (PCSCReset e) {
			throw new CardReset();
		} catch (PCSCException e) {
			throw new CardException(e.getLocalizedMessage(), e);
		}
	}

	protected StandardCardConnection basicConnectShared(StandardCard card,
			int id, ScheduledExecutorService executor) throws CardException {
		try {
			IPCSCContext context = getPcscCardReader().getContext()
					.establishContext();
			PCSCConnection pcscConnection = context.connect(getPcscCardReader()
					.getName(), _IPCSC.SCARD_SHARE_SHARED,
					_IPCSC.SCARD_PROTOCOL_Tx);
			return new StandardCardConnection(card, id, executor, false,
					pcscConnection);
		} catch (PCSCReset e) {
			throw new CardReset();
		} catch (PCSCException e) {
			if (e.getErrorCode() == _PCSC_RETURN_CODES.SCARD_E_SHARING_VIOLATION) {
				throw new CardSharingViolationException();
			}
			throw new CardException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	protected void basicDispose() {
		super.basicDispose();
		getPcscCardReader().removeStatusListener(listenStatus);
	}

	@Override
	public String getName() {
		return getPcscCardReader().getName();
	}

	protected IPCSCCardReader getPcscCardReader() {
		return pcscCardReader;
	}

	private void initialize() throws PCSCException {
		PCSCCardReaderState initialState = getPcscCardReader().getState();
		updateCardState(initialState);
		getPcscCardReader().addStatusListener(listenStatus);
	}

	protected void updateCardState(PCSCCardReaderState newReaderState) {
		EnumCardState cardState = mapToEnumCardState(newReaderState);
		StandardCard tempCard = null;
		StandardCard invalidCard = null;
		synchronized (lock) {
			tempCard = (StandardCard) basicGetCard();
			if (cardState != null) {
				if (tempCard == null) {
					ATR atr = ATR.create(newReaderState.getATR());
					if (atr == null) {
						if (Log.isLoggable(Level.FINEST)) {
							Log.log(Level.FINEST,
									"struct ReaderState does not contain an ATR - event ignored");
						}
					} else {
						tempCard = new StandardCard(this, atr);
					}
				}
			} else if (tempCard != null) {
				invalidCard = tempCard;
				tempCard = null;
			}
			basicSetCard(tempCard);
		}
		// get callbacks out of locked section
		if (invalidCard != null) {
			invalidCard.dispose();
		}
		if (tempCard != null) {
			tempCard.setState(cardState);
		}
	}
}
