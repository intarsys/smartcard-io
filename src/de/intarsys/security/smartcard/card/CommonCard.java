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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.tools.attribute.AttributeMap;
import de.intarsys.tools.concurrent.AbstractFutureTask;
import de.intarsys.tools.concurrent.ThreadTools;

/**
 * Abstract superclass for implementing {@link ICard}.
 * 
 */
abstract public class CommonCard implements ICard {

	public class ConnectTask extends AbstractFutureTask<ICardConnection> {

		private final IConnectionCallback callback;

		private final ScheduledExecutorService executor;

		private final int id;

		private final int protocol;

		protected ConnectTask(int id, int protocol,
				ScheduledExecutorService executor, IConnectionCallback callback) {
			this.id = id;
			this.protocol = protocol;
			this.executor = executor;
			this.callback = callback;
		}

		@Override
		protected ICardConnection compute() throws Exception {
			synchronized (lock) {
				checkValidity();
			}
			if (Log.isLoggable(Level.FINEST)) {
				Log.finest("" + this + " connect shared"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			CommonCardConnection newChannel = basicConnectShared(id, protocol,
					executor);
			if (Log.isLoggable(Level.FINEST)) {
				Log.finest("" + this + " connected"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return newChannel;
		}

		@Override
		protected void taskCancelled() {
			super.taskCancelled();
			if (Log.isLoggable(Level.FINEST)) {
				Log.finest("" + this + " connect cancelled"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			executor.shutdown();
		}

		@Override
		protected void taskFailed() {
			executor.shutdown();
			if (Log.isLoggable(Level.FINEST)) {
				Log.finest("" + this + " connect failed"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (callback != null) {
				final CardException tempEx;
				if (basicGetException() instanceof CardException) {
					tempEx = (CardException) basicGetException();
				} else {
					tempEx = new CardException("unexpected exception", //$NON-NLS-1$
							basicGetException());
				}
				callback.connectionFailed(tempEx);
			}
		}

		@Override
		protected void taskFinished() {
			if (callback != null) {
				callback.connected(basicGetResult());
			}
		}

		@Override
		protected void undo() {
			ICardConnection temp = basicGetResult();
			if (temp == null) {
				return;
			}
			try {
				temp.close(ICardConnection.MODE_LEAVE_CARD);
			} catch (CardException e) {
				Log.log(Level.FINE, "connection close failed", e);
			}
		}
	}

	private final static Logger Log = PACKAGE.Log;

	final private ATR atr;

	final private AttributeMap attributeMap = new AttributeMap();

	final private CommonCardTerminal cardTerminal;

	final protected Object lock = new Object();

	private EnumCardState cardState;

	private static int COUNTER = 0;

	final private int id = COUNTER++;

	protected CommonCard(CommonCardTerminal cardTerminal, ATR atr) {
		assert (cardTerminal != null);
		assert (atr != null);
		this.cardTerminal = cardTerminal;
		this.atr = atr;
		this.cardState = EnumCardState.UNKNOWN;
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " create"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	abstract protected CommonCardConnection basicConnectExclusive(int id,
			int protocol, ScheduledExecutorService executor)
			throws CardException;

	abstract protected CommonCardConnection basicConnectShared(int id,
			int protocol, ScheduledExecutorService executor)
			throws CardSharingViolationException, CardException;

	protected CommonCardTerminal basicGetCardTerminal() {
		return cardTerminal;
	}

	protected void cancelConnectTask(ConnectTask connectTask) {
		if (!connectTask.cancel(false)) {
			try {
				ICardConnection tempChannel = connectTask.get(-1,
						TimeUnit.MILLISECONDS);
				if (tempChannel != null) {
					tempChannel.close(ICardConnection.MODE_LEAVE_CARD);
				}
			} catch (Exception e) {
				//
			}
		}
	}

	protected void checkValidity() throws CardInvalidException {
		if (cardState == EnumCardState.INVALID) {
			throw new CardInvalidException();
		}
	}

	@Override
	final public ICardConnection connectExclusive(int protocol)
			throws CardException {
		synchronized (lock) {
			checkValidity();
		}
		int id = CommonCardConnection.createId();
		CommonCardConnection connection = basicConnectExclusive(id, protocol,
				createExecutor(id));
		return connection;
	}

	@Override
	final public ConnectTask connectShared(int protocol,
			final IConnectionCallback callback) {
		int id = CommonCardConnection.createId();
		ScheduledExecutorService executor = createExecutor(id);
		ConnectTask connectTask = new ConnectTask(id, protocol, executor,
				callback);
		executor.execute(connectTask);
		return connectTask;
	}

	protected ScheduledExecutorService createExecutor(int id) {
		String name = "connection " + id + "-" + getId() + "-"
				+ cardTerminal.getId();
		return Executors.newScheduledThreadPool(1,
				ThreadTools.newThreadFactoryDaemon(name));
	}

	protected void dispose() {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " invalidate"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		setState(EnumCardState.INVALID);
	}

	protected void fromConnectionBeginTransaction(
			CommonCardConnection commonCardConnection) {
		// setState(EnumCardState.CONNECTED_INTRANSACTION);
	}

	@Override
	public ATR getAtr() {
		return atr;
	}

	@Override
	public Object getAttribute(Object key) {
		return attributeMap.getAttribute(key);
	}

	@Override
	public ICardTerminal getCardTerminal() {
		return cardTerminal;
	}

	public int getId() {
		return id;
	}

	@Override
	public EnumCardState getState() {
		synchronized (lock) {
			return cardState;
		}
	}

	@Override
	public boolean isContactless() {
		return getAtr().isContactless();
	}

	@Override
	public Object removeAttribute(Object key) {
		return attributeMap.removeAttribute(key);
	}

	@Override
	public Object setAttribute(Object key, Object value) {
		return attributeMap.setAttribute(key, value);
	}

	protected void setState(EnumCardState newState) {
		EnumCardState oldState;
		synchronized (lock) {
			if (cardState == EnumCardState.INVALID) {
				return;
			}
			if (cardState == newState) {
				return;
			}
			oldState = cardState;
			cardState = newState;
		}
		basicGetCardTerminal().triggerCardEvent(this, oldState, newState);
	}

	@Override
	public String toString() {
		return "card " + id + " in " + getCardTerminal();
	}
}
