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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import de.intarsys.tools.attribute.AttributeMap;
import de.intarsys.tools.concurrent.ThreadTools;
import de.intarsys.tools.event.Event;
import de.intarsys.tools.event.EventDispatcher;
import de.intarsys.tools.event.EventType;
import de.intarsys.tools.event.INotificationListener;

/**
 * Abstract superclass for implementing {@link ICardTerminal}
 * <p>
 * To provide asynchronous communication with cards, the card terminal may
 * delegate tasks to an executor. Typical examples are connection creation or
 * transaction setup, as these may be pending dependent on some external system
 * state.
 * 
 */
abstract public class CommonCardTerminal implements ICardTerminal {

	private static int Counter = 0;

	final private CommonCardSystem cardSystem;

	private EnumCardTerminalState state;

	final protected Object lock = new Object();

	final private EventDispatcher eventDispatcher;

	final private AttributeMap attributes = new AttributeMap();

	private CommonCard card;

	final private int id;

	private final static Logger Log = PACKAGE.Log;

	private final List<Runnable> resourceFinalizers;

	protected CommonCardTerminal(CommonCardSystem cardSystem)
			throws CardException {
		this(Counter++, cardSystem);
	}

	protected CommonCardTerminal(int id, CommonCardSystem cardSystem)
			throws CardException {
		super();
		if (cardSystem == null) {
			throw new CardException("card system required");
		}
		this.id = id;
		this.cardSystem = cardSystem;
		this.resourceFinalizers = new ArrayList<Runnable>(2);
		this.eventDispatcher = new EventDispatcher(this);
		this.state = EnumCardTerminalState.CONNECTED;
	}

	@Override
	public void addNotificationListener(EventType type,
			INotificationListener listener) {
		eventDispatcher.addNotificationListener(type, listener);
	}

	@Override
	public void addSecondaryResourceFinalizer(Runnable finalizer) {
		synchronized (this) {
			resourceFinalizers.add(finalizer);
		}
	}

	abstract protected CommonCardConnection basicConnectDirect(int id,
			ScheduledExecutorService executor) throws CardException;

	protected void basicDispose() {
		freeSecondaryResources();
	}

	protected CommonCard basicGetCard() {
		return card;
	}

	protected void basicSetCard(CommonCard card) {
		this.card = card;
	}

	@Override
	final public ICardConnection connectDirect() throws CardException {
		int id = CommonCardConnection.createId();
		return basicConnectDirect(id, createExecutor(id));
	}

	protected ScheduledExecutorService createExecutor(int id) {
		String name = "connection " + id + "-" + "*" + "-" + getId();
		return Executors.newScheduledThreadPool(1,
				ThreadTools.newThreadFactoryDaemon(name));
	}

	@Override
	final public void dispose() {
		CommonCard tempCard;
		synchronized (lock) {
			if (state == EnumCardTerminalState.INVALID) {
				return;
			}
			state = EnumCardTerminalState.INVALID;
			tempCard = card;
			card = null;
		}
		if (tempCard != null) {
			tempCard.dispose();
		}
		basicDispose();
	}

	@Override
	public void freeSecondaryResources() {
		List<Runnable> tempFinalizers;
		synchronized (this) {
			tempFinalizers = new ArrayList<Runnable>(resourceFinalizers);
			resourceFinalizers.clear();
		}
		for (Runnable finalizer : tempFinalizers) {
			finalizer.run();
		}
	}

	@Override
	public Object getAttribute(Object key) {
		return attributes.getAttribute(key);
	}

	@Override
	public ICard getCard() {
		synchronized (lock) {
			return card;
		}
	}

	@Override
	public CommonCardSystem getCardSystem() {
		return cardSystem;
	}

	protected EventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	public int getId() {
		return id;
	}

	@Override
	public EnumCardTerminalState getState() {
		synchronized (lock) {
			return state;
		}
	}

	@Override
	public boolean isDisposed() {
		return state == EnumCardTerminalState.INVALID;
	}

	@Override
	public Object removeAttribute(Object key) {
		return attributes.removeAttribute(key);
	}

	@Override
	public void removeNotificationListener(EventType type,
			INotificationListener listener) {
		eventDispatcher.removeNotificationListener(type, listener);
	}

	@Override
	public Object setAttribute(Object key, Object value) {
		return attributes.setAttribute(key, value);
	}

	@Override
	public String toString() {
		return "terminal " + id;
	}

	protected void triggerCardEvent(ICard card, EnumCardState oldState,
			EnumCardState newState) {
		final CardEvent event = new CardEvent(card, oldState, newState);
		triggerEvent(event);
	}

	protected void triggerEvent(final Event event) {
		getEventDispatcher().triggerEvent(event);
	}

}
