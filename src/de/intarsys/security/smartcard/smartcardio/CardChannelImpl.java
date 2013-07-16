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

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import de.intarsys.security.smartcard.card.ICardConnection;
import de.intarsys.security.smartcard.card.RequestAPDU;

/**
 * javax.smartcardio internal provider implementation.
 * 
 */
public class CardChannelImpl extends CardChannel {

	final private ICardConnection connection;

	final private CardImpl card;

	final private int number;

	private static final Logger Log = PACKAGE.Log;

	protected CardChannelImpl(CardImpl card, int number,
			ICardConnection connection) {
		super();
		this.card = card;
		this.number = number;
		this.connection = connection;
	}

	@Override
	public void close() throws CardException {
		if (Log.isLoggable(Level.FINE)) {
			Log.log(Level.FINE, getLabel() + " close channel");
		}
		if (getChannelNumber() == 0) {
			throw new IllegalStateException(
					"Cannot close basic logical channel");
		}
		throw new UnsupportedOperationException("channel not yet implemented");
	}

	@Override
	public Card getCard() {
		return card;
	}

	@Override
	public int getChannelNumber() {
		return number;
	}

	protected String getLabel() {
		return "CardChannel " + number + " for " + card.getLabel();
	}

	@Override
	public String toString() {
		return getLabel();
	}

	@Override
	public int transmit(ByteBuffer in, ByteBuffer out) throws CardException {
		byte[] inBytes = new byte[in.remaining()];
		in.get(inBytes);
		RequestAPDU request = new RequestAPDU(inBytes);
		de.intarsys.security.smartcard.card.ResponseAPDU response;
		try {
			response = connection.transmit(request);
		} catch (de.intarsys.security.smartcard.card.CardException e) {
			throw new CardException(e);
		}
		byte[] data = response.getData();
		out.put(response.getData());
		return data.length;
	}

	@Override
	public ResponseAPDU transmit(CommandAPDU apdu) throws CardException {
		RequestAPDU request = new RequestAPDU(apdu.getBytes());
		de.intarsys.security.smartcard.card.ResponseAPDU response;
		try {
			response = connection.transmit(request);
		} catch (de.intarsys.security.smartcard.card.CardException e) {
			throw new CardException(e);
		}
		ResponseAPDU result = new ResponseAPDU(response.getBytes());
		return result;
	}

}
