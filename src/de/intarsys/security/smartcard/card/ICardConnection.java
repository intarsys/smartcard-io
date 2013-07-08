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

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.tools.attribute.IAttributeSupport;

/**
 * The abstraction of a channel or connection opened to a {@link ICard}. A
 * {@link ICardConnection} can be achieved by calling some of the "connect.."
 * methods in {@link ICard}.
 * 
 */
public interface ICardConnection extends ICardTransmitter, IAttributeSupport {

	public static final int MODE_LEAVE_CARD = _IPCSC.SCARD_LEAVE_CARD;
	public static final int MODE_RESET = _IPCSC.SCARD_RESET_CARD;
	public static final int MODE_UNPOWER = _IPCSC.SCARD_UNPOWER_CARD;
	public static final int MODE_EJECT = _IPCSC.SCARD_EJECT_CARD;

	/**
	 * Open a transaction synchronously using a timeout.
	 * 
	 * @param millisecTimeout
	 * @throws CardException
	 * @throws TimeoutException
	 */
	public void beginTransaction(int millisecTimeout) throws TimeoutException,
			CardException, InterruptedException;

	/**
	 * Open an transaction asynchronously.
	 * 
	 * @param transactionCallback
	 *            The callback to be executed upon termination of the operation.
	 * @return A {@link Future} giving access to the ongoing operation.
	 */
	public Future<Void> beginTransaction(
			ITransactionCallback transactionCallback);

	/**
	 * Close an open {@link ICardConnection}. "mode" is one of
	 * {@link ICardConnection#MODE_EJECT },
	 * {@link ICardConnection#MODE_LEAVE_CARD},
	 * {@link ICardConnection#MODE_RESET}, {@link ICardConnection#MODE_UNPOWER}
	 * 
	 * @throws CardException
	 */
	public void close(int mode) throws CardException;

	/**
	 * Send a control request to the card terminal. The request is sent as it is
	 * requested (no mapping is performed).
	 * 
	 * @param controlCode
	 * @param inBuffer
	 * @param inBufferOffset
	 * @param inBufferLength
	 * @param outBufferSize
	 * @return
	 * @throws CardException
	 */
	public byte[] control(int controlCode, byte[] inBuffer, int inBufferOffset,
			int inBufferLength, int outBufferSize) throws CardException;

	/**
	 * Send a control request to the card terminal. The request control code is
	 * adapted according to the environment (Win* or PCSClite).
	 * 
	 * @param controlCode
	 * @param inBuffer
	 * @param inBufferOffset
	 * @param inBufferLength
	 * @param outBufferSize
	 * @return
	 * @throws CardException
	 */
	public byte[] controlMapped(int controlCode, byte[] inBuffer,
			int inBufferOffset, int inBufferLength, int outBufferSize)
			throws CardException;

	/**
	 * End a previously started transaction.
	 * 
	 * @throws CardException
	 */
	public void endTransaction() throws CardException;

	/**
	 * Get attribute data from the card terminal.
	 * 
	 * @param attribId
	 * @return
	 * @throws CardException
	 */
	public byte[] getAttrib(int attribId) throws CardException;

	/**
	 * The {@link ICard} to which we have opened a {@link ICardConnection}.
	 * 
	 * @return The {@link ICard} to which we have opened a
	 *         {@link ICardConnection}. This may return null for a direct
	 *         connection!
	 */
	public ICard getCard();

	/**
	 * The {@link ICardTerminal } to which we have opened a
	 * {@link ICardConnection}. This is important in addition to #getCard,
	 * because we may have a direct connection *without* an {@link ICard}
	 * 
	 * @return The {@link ICardTerminal } to which we have opened a
	 *         {@link ICardConnection}.
	 */
	public ICardTerminal getCardTerminal();

	/**
	 * Experimental - access PCSC State
	 * 
	 */
	public void getStatus() throws CardException;

	/**
	 * @return <code>true</true> if a transaction is active
	 */
	public boolean isTransactionActive();

	/**
	 * @return <code>true</true> if the channel is closed and this object should not be used anymore.
	 */
	public boolean isValid();

	/**
	 * Reconnect an existing connection.
	 * 
	 * @param mode
	 */
	public void reconnect(int mode) throws CardException;

}
