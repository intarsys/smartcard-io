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

import de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES;

/**
 * Monitor the state of an {@link IPCSCCardReader}.
 * 
 * This implementation is running in a thread of its own, using an
 * {@link IPCSCContext} of its own to continuously send (blocking)
 * "getStatusChange" requests. This is the least common denominator for the
 * different platforms and versions of PCSC.
 * 
 */
public class PCSCStatusMonitor {

	public interface IStatusListener {
		public void onException(IPCSCCardReader reader, PCSCException e);

		public void onStatusChange(IPCSCCardReader reader,
				PCSCCardReaderState cardReaderState);
	}

	private final static Logger Log = PACKAGE.Log;

	final private IPCSCCardReader reader;

	private Thread monitorThread;

	private PCSCCardReaderState newReaderState = null;

	private PCSCCardReaderState oldReaderState = null;

	private IPCSCContext pcscContext;

	final private List<IStatusListener> listeners = new ArrayList<>();

	final private Object lock = new Object();

	public PCSCStatusMonitor(IPCSCCardReader cardReader) {
		this.reader = cardReader;
	}

	public void addStatusListener(IStatusListener listener) {
		synchronized (lock) {
			boolean start = listeners.isEmpty();
			listeners.add(listener);
			if (start) {
				start();
			}
		}
	}

	protected IPCSCContext getPcscContext() {
		return pcscContext;
	}

	public IPCSCCardReader getReader() {
		return reader;
	}

	protected boolean monitor() {
		try {
			if (Log.isLoggable(Level.FINEST)) {
				Log.log(Level.FINEST, "" + this + " call getStatusChange()"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			synchronized (lock) {
				if (monitorThread == null) {
					return false;
				}
			}
			newReaderState = getPcscContext().getStatusChange(
					getReader().getName(), oldReaderState, -1);
			if (Log.isLoggable(Level.FINEST)) {
				Log.log(Level.FINEST,
						""		+ this + " call getStatusChange() -> " + newReaderState.toString() + "(" + Integer.toBinaryString(newReaderState.getEventState()) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!newReaderState.isIgnore() && !newReaderState.isUnavailable()) {
				onStatusChange(newReaderState);
				oldReaderState = newReaderState;
			}
		} catch (PCSCException e) {
			if (getPcscContext().isDisposed()) {
				if (Log.isLoggable(Level.FINEST)) {
					Log.log(Level.FINEST, "" + this + " terminated"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return false;
			} else if (e.getErrorCode() == _PCSC_RETURN_CODES.SCARD_E_CANCELLED) {
				if (Log.isLoggable(Level.FINEST)) {
					Log.log(Level.FINEST, "" + this + " cancelled"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return true;
			} else {
				Log.log(Level.FINEST,
						"" + this + " PCSC Exception " + e.getMessage(), e); //$NON-NLS-1$
				oldReaderState = null;
				onException(e);
				return false;
			}
		} catch (TimeoutException e) {
			Log.log(Level.FINEST, "" + this + " Timeout Exception", e); //$NON-NLS-1$ 
		}
		return true;
	}

	protected void monitorLoop() {
		try {
			synchronized (lock) {
				if (monitorThread == null) {
					return;
				}
				pcscContext = getReader().getContext().establishContext();
			}
			while (true) {
				if (!monitor()) {
					break;
				}
			}
		} catch (PCSCException e) {
			Log.log(Level.WARNING, "" + this + " error monitoring status", e); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			stop();
		}
	}

	protected void onException(PCSCException e) {
		List<IStatusListener> temp;
		synchronized (lock) {
			temp = new ArrayList<>(listeners);
		}
		for (IStatusListener listener : temp) {
			listener.onException(getReader(), e);
		}
	}

	protected void onStatusChange(PCSCCardReaderState cardReaderState) {
		List<IStatusListener> temp;
		synchronized (lock) {
			temp = new ArrayList<>(listeners);
		}
		for (IStatusListener listener : temp) {
			listener.onStatusChange(getReader(), cardReaderState);
		}
	}

	public void removeStatusListener(IStatusListener listener) {
		synchronized (lock) {
			if (listeners.remove(listener)) {
				if (listeners.isEmpty()) {
					stop();
				}
			}
		}
	}

	protected void start() {
		synchronized (lock) {
			if (monitorThread != null) {
				return;
			}
			String name = "pcsc monitor " + getReader().getId();
			monitorThread = new Thread(new Runnable() {
				@Override
				public void run() {
					monitorLoop();
				}
			}, name);
			monitorThread.setDaemon(true);
			monitorThread.start();
		}
	}

	/**
	 * Forced shutdown of the PCSC resources.
	 * 
	 */
	public void stop() {
		IPCSCContext temp;
		synchronized (lock) {
			if (monitorThread == null) {
				return;
			}
			monitorThread = null;
			temp = getPcscContext();
			listeners.clear();
		}
		if (temp != null) {
			try {
				temp.dispose();
			} catch (PCSCException e) {
				Log.log(Level.WARNING, "" + this + " error releasing context"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
}
