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
package de.intarsys.security.smartcard.pcsc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * A special context that can elegate to multiple libraries.
 * 
 * This is useful in situations where we dynamically use (manufacturer
 * dependent) PC/SC implementations, for example on a USB stick. No you can
 * control these, together with the standard PC/SC resources, using single
 * {@link IPCSCContext}.
 * 
 */
public class PCSCMultiContext implements IPCSCContext {

	private final List<IPCSCContext> contexts;

	final private IPCSCContextFactory system;

	private boolean disposed = false;

	public PCSCMultiContext(IPCSCContextFactory system,
			List<IPCSCContext> contexts) {
		this.system = system;
		this.contexts = contexts;
	}

	public void cancelGetStatusChange() throws PCSCException {
	}

	@Override
	public IPCSCConnection connect(String readerName, int shareMode,
			int protocol) throws PCSCException {
		return null;
	}

	@Override
	public void dispose() {
		synchronized (this) {
			if (disposed) {
				return;
			}
			disposed = true;
		}
		for (IPCSCContext context : contexts) {
			try {
				context.dispose();
			} catch (PCSCException e) {
				// ignore ?
			}
		}
	}

	@Override
	public IPCSCContext establishContext() throws PCSCException {
		return system.establishContext();
	}

	@Override
	public PCSCCardReaderState getStatusChange(String readerName,
			PCSCCardReaderState currentState, int millisecTimeout)
			throws PCSCException, TimeoutException {
		return null;
	}

	@Override
	public boolean isDisposed() {
		synchronized (this) {
			return disposed;
		}
	}

	@Override
	public List<IPCSCCardReader> listReaders() throws PCSCException {
		List<IPCSCCardReader> readers = new ArrayList<IPCSCCardReader>();
		for (IPCSCContext context : contexts) {
			readers.addAll(context.listReaders());
		}
		return readers;
	}

}
