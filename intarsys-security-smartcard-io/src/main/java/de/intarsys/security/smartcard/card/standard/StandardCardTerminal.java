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
package de.intarsys.security.smartcard.card.standard;

import java.util.concurrent.ScheduledExecutorService;

import de.intarsys.security.smartcard.card.ATR;
import de.intarsys.security.smartcard.card.CardException;
import de.intarsys.security.smartcard.card.CommonCardConnection;
import de.intarsys.security.smartcard.card.CommonCardTerminal;
import de.intarsys.security.smartcard.card.EnumCardState;
import de.intarsys.security.smartcard.pcsc.IPCSCCardReader;
import de.intarsys.security.smartcard.pcsc.IPCSCConnection;
import de.intarsys.security.smartcard.pcsc.IPCSCContext;
import de.intarsys.security.smartcard.pcsc.PCSCCardReaderState;
import de.intarsys.security.smartcard.pcsc.PCSCException;
import de.intarsys.security.smartcard.pcsc.PCSCStatusMonitor;
import de.intarsys.security.smartcard.pcsc.PCSCTools;
import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.tools.event.INotificationSupport;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * The abstraction of a single terminal, optionally containing a token,
 * connected to the system.
 * <p>
 * The standard implementation maps directly to the native PCSC API. The initial
 * {@link IPCSCContext} is defined by the {@link IPCSCCardReader} instance.
 * 
 * Monitoring the card reader is started immediately in a thread of its own.
 * 
 * Upon connecting, this implementation will create a new {@link IPCSCContext}
 * AND a {@link IPCSCConnection}. This is done for maximum decoupling and PCSC
 * platform and version independence (some platforms may serialize requests to
 * the same context).
 * <p>
 * {@link StandardCardTerminal} is used in a multithreaded environment.
 */
public class StandardCardTerminal extends CommonCardTerminal implements INotificationSupport {

	private static final ILogger Log = PACKAGE.Log;

	public static EnumCardState mapToEnumCardState(PCSCCardReaderState readerState) {
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

	private final PCSCStatusMonitor monitor;

	private final PCSCStatusMonitor.IStatusListener listenStatus = new PCSCStatusMonitor.IStatusListener() {
		@Override
		public void onException(IPCSCCardReader reader, PCSCException e) {
			dispose();
		}

		@Override
		public void onStatusChange(IPCSCCardReader reader, PCSCCardReaderState cardReaderState) {
			updateCardState(cardReaderState);
		}
	};

	private final IPCSCCardReader pcscCardReader;

	protected StandardCardTerminal(StandardCardSystem cardSystem, IPCSCCardReader pcscCardReader) throws CardException {
		super(cardSystem, pcscCardReader.getId());
		this.pcscCardReader = pcscCardReader;
		PCSCCardReaderState initialState;
		try {
			initialState = getPcscCardReader().getState();
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
		updateCardState(initialState);
		monitor = new PCSCStatusMonitor(pcscCardReader);
		monitor.addStatusListener(listenStatus);
		Log.info("{} created for '{}'", this, getName());
	}

	@Override
	protected CommonCardConnection basicConnectDirect(String suffix, ScheduledExecutorService executor)
			throws CardException {
		IPCSCContext context = null;
		try {
			context = getPcscCardReader().getContext().establishContext();
			Log.trace("{} create connection context {}", getLogLabel(), context);
			IPCSCConnection connection = context.connect(suffix, getPcscCardReader().getName(),
					_IPCSC.SCARD_SHARE_DIRECT,
					PCSCTools.DirectConnectProtocol);
			StandardCardConnection newChannel = new StandardCardConnection(this, executor, true, connection);
			return newChannel;
		} catch (PCSCException e) {
			if (context != null) {
				try {
					context.dispose();
				} catch (PCSCException ignore) {
					//
				}
			}
			throw CardException.create(e);
		}
	}

	protected StandardCardConnection basicConnectExclusive(StandardCard card, String suffix, int protocol,
			ScheduledExecutorService executor) throws CardException {
		IPCSCContext context = null;
		try {
			context = getPcscCardReader().getContext().establishContext();
			Log.trace("{} create connection context {}", getLogLabel(), context);
			IPCSCConnection pcscConnection = context.connect(suffix, getPcscCardReader().getName(),
					_IPCSC.SCARD_SHARE_EXCLUSIVE, protocol);
			return new StandardCardConnection(card, executor, true, pcscConnection);
		} catch (PCSCException e) {
			if (context != null) {
				try {
					context.dispose();
				} catch (PCSCException ignore) {
					//
				}
			}
			throw CardException.create(e);
		}
	}

	protected StandardCardConnection basicConnectShared(StandardCard card, String suffix, int protocol,
			ScheduledExecutorService executor) throws CardException {
		IPCSCContext context = null;
		try {
			context = getPcscCardReader().getContext().establishContext();
			Log.trace("{} create connection context {}", getLogLabel(), context);
			IPCSCConnection pcscConnection = context.connect(suffix, getPcscCardReader().getName(),
					_IPCSC.SCARD_SHARE_SHARED, protocol);
			return new StandardCardConnection(card, executor, false, pcscConnection);
		} catch (PCSCException e) {
			if (context != null) {
				try {
					context.dispose();
				} catch (PCSCException ignore) {
					//
				}
			}
			throw CardException.create(e);
		}
	}

	@Override
	protected void basicDispose() {
		super.basicDispose();
		monitor.removeStatusListener(listenStatus);
	}

	@Override
	public String getName() {
		return getPcscCardReader().getName();
	}

	protected IPCSCCardReader getPcscCardReader() {
		return pcscCardReader;
	}

	@Override
	public void renew() {
		EnumCardState cardState = null;
		StandardCard tempCard = null;
		StandardCard newCard = null;
		synchronized (lock) {
			tempCard = (StandardCard) basicGetCard();
			if (tempCard == null) {
				return;
			}
			cardState = tempCard.getState();
			ATR atr = tempCard.getAtr();
			newCard = new StandardCard(this, atr);
			basicSetCard(newCard);
		}
		tempCard.dispose();
		newCard.setState(cardState);
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
						Log.debug("{} struct ReaderState does not contain an ATR - event ignored", this);
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
