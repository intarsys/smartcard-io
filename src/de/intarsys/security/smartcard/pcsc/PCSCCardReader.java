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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The default {@link IPCSCCardReader} implementation.
 * 
 */
public class PCSCCardReader implements IPCSCCardReader {

	private static final Logger Log = PACKAGE.Log;

	private static int Counter = 0;

	final private List<IStatusListener> listeners = new ArrayList<>();

	final private PCSCStatusMonitor monitor = new PCSCStatusMonitor(this);

	final private String name;

	final private IPCSCContext context;

	final private Object lock = new Object();

	final private int id;

	public PCSCCardReader(IPCSCContext context, String readerName) {
		this.id = Counter++;
		this.context = context;
		this.name = readerName;
		Log.log(Level.FINE, "created PCSCCardReader " + readerName + " as ["
				+ id + "]");
	}

	@Override
	public void addStatusListener(IStatusListener listener) {
		synchronized (lock) {
			boolean start = listeners.isEmpty();
			listeners.add(listener);
			if (start) {
				getMonitor().start();
			}
		}
	}

	@Override
	public IPCSCContext getContext() {
		return context;
	}

	@Override
	public int getId() {
		return id;
	}

	protected PCSCStatusMonitor getMonitor() {
		return monitor;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public PCSCCardReaderState getState() throws PCSCException {
		try {
			return getContext().getStatusChange(this.getName(), null, 0);
		} catch (TimeoutException e) {
			// will not happen
			return PCSCCardReaderState.UNAWARE;
		}
	}

	protected void onException(PCSCException e) {
		List<IStatusListener> temp;
		synchronized (lock) {
			temp = new ArrayList<>(listeners);
		}
		for (IStatusListener listener : temp) {
			listener.onException(e);
		}
	}

	protected void onStatusChange(PCSCCardReaderState cardReaderState) {
		List<IStatusListener> temp;
		synchronized (lock) {
			temp = new ArrayList<>(listeners);
		}
		for (IStatusListener listener : temp) {
			listener.onStatusChange(cardReaderState);
		}
	}

	@Override
	public void removeStatusListener(IStatusListener listener) {
		synchronized (lock) {
			if (listeners.remove(listener)) {
				if (listeners.isEmpty()) {
					getMonitor().stop();
				}
			}
		}
	}

	@Override
	public String toString() {
		return "pcscreader " + id;
	}

}
