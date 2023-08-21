/*
 * Copyright (c) 2013, intarsys GmbH
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

import de.intarsys.security.smartcard.card.CardException;
import de.intarsys.security.smartcard.card.CommonCardConnection;
import de.intarsys.security.smartcard.card.RequestAPDU;
import de.intarsys.security.smartcard.card.ResponseAPDU;
import de.intarsys.security.smartcard.pcsc.IPCSCConnection;
import de.intarsys.security.smartcard.pcsc.PCSCException;
import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * The abstraction of a connection opened to a {@link StandardCard}. A
 * {@link StandardCardConnection} can be achieved by calling some of the
 * "connect.." methods in {@link StandardCard}.
 * <p>
 * {@link StandardCardConnection} is used in a multithreaded environment and its
 * state is changed by either one of the multiple clients working with the
 * {@link StandardCard} or the {@link StandardCardTerminal} controlling its
 * state.
 * <p>
 * {@link StandardCard} is part of the abstraction layer that is built on top of
 * the PCSC API. From a PCSC point of view, both a PCSC context AND a PCSC
 * connection are made for a new StandardCardConnection. This is the least
 * common denominator for different PCSC platforms and versions. Again, this is
 * why both the connection and the context are disposed when closing the
 * connection.
 * 
 */
public class StandardCardConnection extends CommonCardConnection {

	private static final ILogger Log = PACKAGE.Log;

	private final IPCSCConnection pcscConnection;

	protected StandardCardConnection(StandardCard pCard,
			ScheduledExecutorService executorTask, boolean exclusive,
			IPCSCConnection pPcscConnection) {
		super(pCard, pPcscConnection.getId(), executorTask, exclusive);
		this.pcscConnection = pPcscConnection;
		Log.debug("{} created for {}", this, pcscConnection); //$NON-NLS-1$
	}

	protected StandardCardConnection(StandardCardTerminal pCardTerminal, ScheduledExecutorService executorTask,
			boolean exclusive, IPCSCConnection pPcscConnection) {
		super(pCardTerminal, pPcscConnection.getId(), executorTask, exclusive);
		this.pcscConnection = pPcscConnection;
		Log.debug("{} created for {}", this, pcscConnection); //$NON-NLS-1$
	}

	@Override
	protected void basicBeginTransaction() throws CardException {
		try {
			pcscConnection.beginTransaction();
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
		super.basicBeginTransaction();
	}

	@Override
	protected void basicClose(int mode) throws CardException {
		try {
			pcscConnection.disconnect(mode);
		} catch (Exception e) {
			//
		}
		try {
			pcscConnection.getContext().dispose();
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
	}

	@Override
	protected byte[] basicControl(int controlCode, byte[] inBuffer,
			int inBufferOffset, int inBufferLength, int outBufferSize)
			throws CardException {
		try {
			return pcscConnection.control(controlCode, inBuffer,
					inBufferOffset, inBufferLength, outBufferSize);
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
	}

	@Override
	protected byte[] basicControlMapped(int controlCode, byte[] inBuffer,
			int inBufferOffset, int inBufferLength, int outBufferSize)
			throws CardException {
		try {
			return pcscConnection.controlMapped(controlCode, inBuffer,
					inBufferOffset, inBufferLength, outBufferSize);
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
	}

	@Override
	protected void basicEndTransaction() throws CardException {
		try {
			pcscConnection.endTransaction(_IPCSC.SCARD_LEAVE_CARD);
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
	}

	@Override
	protected byte[] basicGetAttrib(int attribId) throws CardException {
		try {
			return pcscConnection.getAttrib(attribId);
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
	}

	@Override
	protected StandardCard basicGetCard() {
		return (StandardCard) super.basicGetCard();
	}

	@Override
	protected void basicGetStatus() throws CardException {
		try {
			pcscConnection.getStatus();
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
	}

	@Override
	protected void basicReconnect(int mode) throws CardException {
		try {
			pcscConnection.reconnect(pcscConnection.getShareMode(),
					pcscConnection.getProtocol(), mode);
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
	}

	@Override
	protected ResponseAPDU basicTransmit(RequestAPDU request)
			throws CardException {
		try {
			byte[] response = pcscConnection.transmit(request.getBytes(), 0,
					request.getLength(), request.getReceiveLength(),
					request.isSensitiveContent());
			return new ResponseAPDU(response);
		} catch (PCSCException e) {
			throw CardException.create(e);
		}
	}

	public IPCSCConnection getPcscConnection() {
		return pcscConnection;
	}

	@Override
	public int getProtocol() {
		return pcscConnection.getProtocol();
	}
}
