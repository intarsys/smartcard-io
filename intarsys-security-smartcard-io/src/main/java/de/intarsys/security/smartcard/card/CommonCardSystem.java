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
package de.intarsys.security.smartcard.card;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.annotation.PreDestroy;

import de.intarsys.security.smartcard.card.standard.StandardCardTerminal;
import de.intarsys.tools.concurrent.ThreadTools;
import de.intarsys.tools.event.AttributeChangedEvent;
import de.intarsys.tools.event.DeferredEventDispatcher;
import de.intarsys.tools.event.Event;
import de.intarsys.tools.event.EventType;
import de.intarsys.tools.event.INotificationListener;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * Abstract superclass for implementing {@link ICardSystem}.
 * 
 */
public abstract class CommonCardSystem implements ICardSystem {

	private static final ILogger Log = PACKAGE.Log;

	private static final int POLLING_INTERVAL = 500;

	private final Object lock = new Object();

	private final DeferredEventDispatcher eventDispatcher;

	private ScheduledExecutorService scheduledExecutor;

	private ScheduledFuture<?> scheduledUpdate;

	private boolean disposed;

	private boolean enabled = true;

	private final Runnable updateCardTerminalsCall = new Runnable() {
		@Override
		public void run() {
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				@Override
				public Object run() {
					updateCardTerminals();
					return null;
				}
			});
		}
	};

	private final Map<String, ICardTerminal> cardTerminalMap;

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
	@PreDestroy
	public void dispose() {
		final ICardTerminal[] terminals;
		synchronized (lock) {
			if (disposed) {
				return;
			}
			Log.trace("{} dispose", this); //$NON-NLS-1$ //$NON-NLS-2$
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

	public boolean isEnabled() {
		return enabled;
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

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	protected void startEventScheduler() {
		Log.info("{} start terminal monitor", this); //$NON-NLS-1$
		scheduledExecutor = Executors
				.newSingleThreadScheduledExecutor(ThreadTools
						.newThreadFactoryDaemon("CardSystem terminal monitor")); //$NON-NLS-1$
		scheduledUpdate = scheduledExecutor.scheduleWithFixedDelay(
				updateCardTerminalsCall, 0, POLLING_INTERVAL, MILLISECONDS);
	}

	protected void stopEventScheduler() {
		Log.debug("{} stop terminal monitor", this); //$NON-NLS-1$
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
					Log.info("{} add {}", this, terminal); //$NON-NLS-1$
					cardTerminalMap.put(terminal.getName(), terminal);
					triggerEvent(new AttributeChangedEvent(this,
							ICardSystem.ATTR_CARD_TERMINALS, null, terminal));
				}
				for (ICardTerminal terminal : oldTerminals.values()) {
					// this terminal no longer available
					Log.info("{} remove {}", this, terminal); //$NON-NLS-1$
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
			Log.warn("{} update card terminals unexpected exception", this, e); //$NON-NLS-1$
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
