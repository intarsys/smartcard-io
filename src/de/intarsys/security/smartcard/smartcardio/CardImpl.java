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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;

import de.intarsys.security.smartcard.card.ICardConnection;
import de.intarsys.security.smartcard.card.ICardTerminal;

/**
 * javax.smartcardio internal provider implementation.
 * 
 */
public class CardImpl extends Card {

	final private ICardConnection connection;

	final private CardChannelImpl cardChannel;

	private static final Logger Log = PACKAGE.Log;

	protected CardImpl(ICardConnection connection) {
		super();
		this.connection = connection;
		this.cardChannel = new CardChannelImpl(this, 0, connection);
	}

	@Override
	public void beginExclusive() throws CardException {
		Future<Void> f = connection.beginTransaction(null);
		try {
			f.get();
		} catch (InterruptedException e) {
			throw new CardException(e);
		} catch (ExecutionException e) {
			throw new CardException(e.getCause());
		}
	}

	@Override
	public void disconnect(boolean reset) throws CardException {
		try {
			connection.close(reset ? ICardConnection.MODE_RESET
					: ICardConnection.MODE_LEAVE_CARD);
		} catch (de.intarsys.security.smartcard.card.CardException e) {
			throw new CardException(e);
		}
	}

	@Override
	public void endExclusive() throws CardException {
		try {
			connection.endTransaction();
		} catch (de.intarsys.security.smartcard.card.CardException e) {
			throw new CardException(e);
		}
	}

	@Override
	public ATR getATR() {
		return new ATR(connection.getCard().getAtr().getBytes());
	}

	@Override
	public CardChannel getBasicChannel() {
		return cardChannel;
	}

	protected String getLabel() {
		return "Card " + connection.toString();
	}

	@Override
	public String getProtocol() {
		int p = connection.getProtocol();
		if (p == ICardTerminal.PROTOCOL_T0) {
			return "T=0";
		} else if (p == ICardTerminal.PROTOCOL_T1) {
			return "T=1";
		} else {
			return "unknown=" + p;
		}
	}

	@Override
	public CardChannel openLogicalChannel() throws CardException {
		if (Log.isLoggable(Level.FINE)) {
			Log.log(Level.FINE, getLabel() + " open channel");
		}
		throw new CardException("unsupported operation");
	}

	@Override
	public String toString() {
		return connection.toString();
	}

	@Override
	public byte[] transmitControlCommand(int controlCode, byte[] inBuffer)
			throws CardException {
		try {
			return connection.control(controlCode, inBuffer, 0,
					inBuffer.length, 0);
		} catch (de.intarsys.security.smartcard.card.CardException e) {
			throw new CardException(e);
		}
	}

}
