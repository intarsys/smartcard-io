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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;

/**
 * Monitor the state of a {@link CardTerminal}.
 * 
 * This is a simple helper class to create a listener type API for
 * {@link CardTerminal}.
 * 
 */
public class CardTerminalMonitor {
	public interface ICardTerminalListener {

		public void onException(CardTerminal terminal, CardException e);

		public void onStatusChange(CardTerminal terminal, boolean present);
	}

	private static final Logger Log = PACKAGE.Log;

	final private CardTerminal terminal;

	private Thread monitorThread;

	final private List<ICardTerminalListener> listeners = new ArrayList<>();

	final private Object lock = new Object();

	private boolean newState;

	private boolean oldState;

	public CardTerminalMonitor(CardTerminal terminal) {
		this.terminal = terminal;
	}

	public void addStatusListener(ICardTerminalListener listener) {
		synchronized (lock) {
			boolean start = listeners.isEmpty();
			listeners.add(listener);
			if (start) {
				start();
			}
		}
	}

	public CardTerminal getTerminal() {
		return terminal;
	}

	protected boolean monitor() throws CardException {
		synchronized (lock) {
			if (monitorThread == null) {
				return false;
			}
		}
		if (oldState) {
			terminal.waitForCardAbsent(0);
		} else {
			terminal.waitForCardPresent(0);
		}
		newState = terminal.isCardPresent();
		if (oldState != newState) {
			onStatusChange(newState);
			oldState = newState;
		}
		return true;
	}

	protected void monitorLoop() {
		try {
			oldState = getTerminal().isCardPresent();
			while (monitor()) {
			}
		} catch (CardException e) {
			onException(e);
		} finally {
			stop();
		}
	}

	protected void onException(CardException e) {
		List<ICardTerminalListener> temp;
		synchronized (lock) {
			temp = new ArrayList<>(listeners);
		}
		for (ICardTerminalListener listener : temp) {
			listener.onException(getTerminal(), e);
		}
	}

	protected void onStatusChange(boolean state) {
		List<ICardTerminalListener> temp;
		synchronized (lock) {
			temp = new ArrayList<>(listeners);
		}
		for (ICardTerminalListener listener : temp) {
			listener.onStatusChange(getTerminal(), state);
		}
	}

	public void removeStatusListener(ICardTerminalListener listener) {
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
			String name = "card terminal monitor " + getTerminal().getName();
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

	protected void stop() {
		synchronized (lock) {
			if (monitorThread == null) {
				return;
			}
			monitorThread = null;
			listeners.clear();
		}
	}
}
