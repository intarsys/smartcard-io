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
package de.intarsys.security.smartcard.pcsc;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * The iconified PC/SC context.
 * 
 * This is created by some {@link IPCSCContextFactory}, available at
 * {@link PCSCContextFactory#get()}
 * 
 */
public interface IPCSCContext extends IPCSCContextFactory {

	/**
	 * Open an {@link IPCSCConnection}.
	 * 
	 * @param id
	 * @param readerName
	 * @param shareMode
	 * @param protocol
	 * @return
	 * @throws PCSCException
	 */
	public IPCSCConnection connect(String id, String readerName, int shareMode, int protocol) throws PCSCException;

	/**
	 * Dispose the PC/SC context.
	 * 
	 * @throws PCSCException
	 */
	public abstract void dispose() throws PCSCException;

	/**
	 * Get a {@link PCSCCardReaderState} for the context and the selected
	 * reader.
	 * 
	 * @param readerName
	 * @param currentState
	 * @param millisecTimeout
	 * @return
	 * @throws PCSCException
	 * @throws TimeoutException
	 */
	public PCSCCardReaderState getStatusChange(String readerName, PCSCCardReaderState currentState, int millisecTimeout)
			throws PCSCException, TimeoutException;

	/**
	 * <code>true</code> if this object is already disposed.
	 * 
	 * @return <code>true</code> if this object is already disposed.
	 */
	public boolean isDisposed();

	/**
	 * Enumerate all readers.
	 * 
	 * @return
	 * @throws PCSCException
	 */
	public abstract List<IPCSCCardReader> listReaders() throws PCSCException;

}