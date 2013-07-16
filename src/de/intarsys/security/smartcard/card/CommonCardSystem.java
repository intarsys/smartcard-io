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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.security.smartcard.card.standard.StandardCardTerminal;
import de.intarsys.tools.concurrent.ThreadTools;
import de.intarsys.tools.event.AttributeChangedEvent;
import de.intarsys.tools.event.DeferredEventDispatcher;
import de.intarsys.tools.event.Event;
import de.intarsys.tools.event.EventType;
import de.intarsys.tools.event.INotificationListener;

/**
 * Abstract superclass for implementing {@link ICardSystem}.
 * 
 */
abstract public class CommonCardSystem implements ICardSystem {

	final private Object lock = new Object();

	final private DeferredEventDispatcher eventDispatcher;

	private ScheduledExecutorService scheduledExecutor;

	private ScheduledFuture<?> scheduledUpdate;

	private boolean disposed;

	private final static Logger Log = PACKAGE.Log;

	final private Runnable updateCardTerminalsCall = new Runnable() {
		@Override
		public void run() {
			updateCardTerminals();
		}
	};

	private final static int POLLING_INTERVAL = 500;

	final private Map<String, ICardTerminal> cardTerminalMap;

	public CommonCardSystem() {
		this.eventDispatcher = new DeferredEventDispatcher(this);
		this.disposed = false;
		this.cardTerminalMap = new HashMap<String, ICardTerminal>();
	}

	@Override
	public void addNotificationListener(EventType type,
			INotificationListener listener) {
		synchronized (lock) {
			if (!eventDispatcher.hasListener()) {
				eventDispatcher.addNotificationListener(type, listener);
				// start event scheduler after registration!!
				startEventScheduler();
			} else {
				eventDispatcher.addNotificationListener(type, listener);
			}
		}
	}

	protected void basicDispose() {
	}

	@Override
	public void dispose() {
		final ICardTerminal[] terminals;
		synchronized (lock) {
			if (disposed) {
				return;
			}
			if (Log.isLoggable(Level.FINEST)) {
				Log.log(Level.FINEST, "" + this + " dispose"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			disposed = true;
			terminals = cardTerminalMap.values().toArray(
					new StandardCardTerminal[cardTerminalMap.size()]);
			cardTerminalMap.clear();
			stopEventScheduler();
			basicDispose();
		}
		// FEATURE insane race condition
		// when we have a card transaction and release
		// 1) transaction
		// 2) connection
		// a third party application may connect a (long running) transaction in
		// between - effectively locking us from issuing the disconnect, which
		// then prevents application shutdown.
		//
		// Therefore we do NOT try a clean shutdown -we destroy (PCSC) resources
		// and go on...
		//
		for (ICardTerminal terminal : terminals) {
			terminal.dispose();
			triggerEvent(new AttributeChangedEvent(this,
					ICardSystem.ATTR_CARD_TERMINALS, terminal, null));
		}
		// events are queued
		flushEvents();
	}

	protected void flushEvents() {
		eventDispatcher.flush();
	}

	@Override
	public ICardTerminal getCardTerminal(String name) {
		synchronized (lock) {
			return cardTerminalMap.get(name);
		}
	}

	/**
	 * An array of the card terminals currently connected to the system.
	 * 
	 * @return An array of the card terminals currently connected to the system.
	 */
	@Override
	public ICardTerminal[] getCardTerminals() {
		updateCardTerminals();
		synchronized (lock) {
			return cardTerminalMap.values().toArray(
					new ICardTerminal[cardTerminalMap.size()]);
		}
	}

	@Override
	public boolean isDisposed() {
		synchronized (lock) {
			return disposed;
		}
	}

	@Override
	public void removeNotificationListener(EventType type,
			INotificationListener listener) {
		synchronized (lock) {
			eventDispatcher.removeNotificationListener(type, listener);
			if (!eventDispatcher.hasListener()) {
				stopEventScheduler();
			}
		}
	}

	protected void startEventScheduler() {
		if (Log.isLoggable(Level.FINEST)) {
			Log.log(Level.FINEST, "" + this + " launch CardSystem event thread"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		scheduledExecutor = Executors
				.newSingleThreadScheduledExecutor(ThreadTools
						.newThreadFactoryDaemon("CardSystem terminal monitor")); //$NON-NLS-1$
		scheduledUpdate = scheduledExecutor.scheduleWithFixedDelay(
				updateCardTerminalsCall, 0, POLLING_INTERVAL, MILLISECONDS);
	}

	protected void stopEventScheduler() {
		if (Log.isLoggable(Level.FINEST)) {
			Log.log(Level.FINEST, "" + this + " stop event scheduler"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (scheduledUpdate != null) {
			scheduledUpdate.cancel(true);
			scheduledUpdate = null;
			scheduledExecutor.shutdownNow();
		}
	}

	protected void triggerEvent(Event event) {
		eventDispatcher.handleEvent(event);
	}

	protected void updateCardTerminals() {
		try {
			Map<String, ICardTerminal> oldTerminals;
			Map<String, ICardTerminal> newTerminals;
			synchronized (lock) {
				if (disposed) {
					return;
				}
				oldTerminals = new HashMap<String, ICardTerminal>(
						cardTerminalMap);
				newTerminals = new HashMap<String, ICardTerminal>();
				//
				updateCardTerminals(oldTerminals, newTerminals);
				//
				for (ICardTerminal terminal : newTerminals.values()) {
					if (Log.isLoggable(Level.FINEST)) {
						Log.log(Level.FINEST,
								""		+ this + " add terminal " + terminal.getName()); //$NON-NLS-1$ //$NON-NLS-2$
					}
					cardTerminalMap.put(terminal.getName(), terminal);
					triggerEvent(new AttributeChangedEvent(this,
							ICardSystem.ATTR_CARD_TERMINALS, null, terminal));
				}
				for (ICardTerminal terminal : oldTerminals.values()) {
					// this terminal no longer available
					if (Log.isLoggable(Level.FINEST)) {
						Log.log(Level.FINEST,
								""		+ this + " remove terminal " + terminal.getName()); //$NON-NLS-1$ //$NON-NLS-2$
					}
					cardTerminalMap.remove(terminal.getName());
					triggerEvent(new AttributeChangedEvent(this,
							ICardSystem.ATTR_CARD_TERMINALS, terminal, null));
				}
			}
			for (ICardTerminal terminal : oldTerminals.values()) {
				// do not hold lock when calling dispose
				terminal.dispose();
			}
			// events are queued to ensure sequence and lock free execution
			flushEvents();
		} catch (Exception e) {
			// keep alive!
			Log.log(Level.WARNING,
					"update card terminals unexpected exception", e); //$NON-NLS-1$
		}
	}

	/**
	 * Prepare the collections of old and new terminals.
	 * 
	 * Upon call, oldTerminals holds the collection of previously known
	 * terminals. newTerminals hold an empty collection. When returning,
	 * newTerminals holds all available terminals that are not known previously,
	 * oldTerminals holds all terminals no longer available.
	 * 
	 * @param oldTerminals
	 * @param newTerminals
	 */
	protected void updateCardTerminals(Map<String, ICardTerminal> oldTerminals,
			Map<String, ICardTerminal> newTerminals) {
	}

}
