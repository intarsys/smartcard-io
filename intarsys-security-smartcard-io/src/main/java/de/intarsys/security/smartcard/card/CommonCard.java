/*
 * Copyright (c) 2013, intarsys GmbH
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - Neither the name of intarsys nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific
 * prior written permission.
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
import java.util.concurrent.ScheduledExecutorService;

import de.intarsys.tools.attribute.AttributeMap;
import de.intarsys.tools.concurrent.AbstractFutureTask;
import de.intarsys.tools.concurrent.ITaskCallback;
import de.intarsys.tools.exception.ExceptionTools;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * Abstract superclass for implementing {@link ICard}.
 * 
 */
public abstract class CommonCard implements ICard {

	public class ConnectTask extends AbstractFutureTask<ICardConnection> {

		private final ScheduledExecutorService executor;

		private final String suffix;

		private final int protocol;

		private CommonCardConnection connection;

		protected ConnectTask(String suffix, int protocol, ScheduledExecutorService executor) {
			super();
			this.suffix = suffix;
			this.protocol = protocol;
			this.executor = executor;
			created();
		}

		@Override
		protected ICardConnection compute() throws Exception {
			synchronized (lock) {
				// maybe card already invalidated, spare me PCSC roundtrip
				checkValidity();
			}
			Log.trace("{} {} connect shared", getLabel(), CommonCard.this); //$NON-NLS-1$
			connection = basicConnectShared(suffix, protocol, executor);
			// this may fail!!
			addConnection(connection);
			return connection;
		}

		@Override
		protected void taskFailed() {
			if (connection != null) {
				// we established a connection, but upon success the card is
				// already invalid
				try {
					connection.close(ICardConnection.MODE_LEAVE_CARD);
				} catch (CardException e) {
					//
				}
			}
			executor.shutdown();
			Log.debug("{} {} connect {}", getLabel(), CommonCard.this, (isCancelled() ? "canceled" : "failed")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			super.taskFailed();
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
				Log.trace("{} {} close failed ({})", getLabel(), temp, ExceptionTools.getMessage(e));
			}
		}
	}

	private static final ILogger Log = PACKAGE.Log;

	private final ATR atr;

	private final AttributeMap attributeMap = new AttributeMap();

	private final CommonCardTerminal cardTerminal;

	protected final Object lock = new Object();

	private EnumCardState cardState;

	private final String id;

	private List<CommonCardConnection> connections = new ArrayList<>();

	protected CommonCard(CommonCardTerminal cardTerminal, ATR atr) {
		assert (cardTerminal != null);
		assert (atr != null);
		this.cardTerminal = cardTerminal;
		this.id = cardTerminal.getId() + "-" + CardTools.createId();
		this.atr = atr;
		this.cardState = EnumCardState.UNKNOWN;
		Log.debug("{} with ATR '{}' in {} created", getLogLabel(), atr, cardTerminal);
	}

	protected void addConnection(CommonCardConnection connection) throws CardException {
		synchronized (lock) {
			/*
			 * our card is a synthetic construct, PCSC deals with contexts. so
			 * it is possible that a connection suceeds while we deem the
			 * corresponding card as invalid in the meantime. to maintain
			 * integrity we must *close* the connection.
			 */
			checkValidity();
			connections.add(connection);
		}
	}

	protected abstract CommonCardConnection basicConnectExclusive(String id, int protocol,
			ScheduledExecutorService executor) throws CardException;

	protected abstract CommonCardConnection basicConnectShared(String id, int protocol,
			ScheduledExecutorService executor) throws CardException;

	protected CommonCardTerminal basicGetCardTerminal() {
		return cardTerminal;
	}

	protected void checkValidity() throws CardException {
		if (cardState == EnumCardState.INVALID) {
			throw new CardUnavailable();
		}
		if (basicGetCardTerminal() != null) {
			basicGetCardTerminal().checkValidity();
		}
	}

	@Override
	public final ICardConnection connectExclusive(int protocol) throws CardException {
		synchronized (lock) {
			checkValidity();
		}
		String suffix = CardTools.createId();
		String executorId = getId() + "-" + suffix;
		ScheduledExecutorService executor = CardTools.createExecutor(executorId);
		return basicConnectExclusive(suffix, protocol, executor);
	}

	@Override
	public final ConnectTask connectShared(int protocol, final ITaskCallback<ICardConnection> callback) {
		String suffix = CardTools.createId();
		String executorId = getId() + "-" + suffix;
		ScheduledExecutorService executor = CardTools.createExecutor(executorId);
		ConnectTask connectTask = new ConnectTask(suffix, protocol, executor);
		if (callback != null) {
			connectTask.addTaskCallback(callback);
		}
		executor.execute(connectTask);
		return connectTask;
	}

	protected void dispose() {
		Log.debug("{} dispose", getLogLabel()); //$NON-NLS-1$ //$NON-NLS-2$
		// we *may* be disposed with connections active! we must close these
		// as later on we will have no longer access and connections are
		// assigned
		// uniquely to cards in our model.
		List<CommonCardConnection> tempConnections = getConnections();
		for (CommonCardConnection connection : tempConnections) {
			try {
				connection.close(ICardConnection.MODE_LEAVE_CARD);
			} catch (CardException e) {
				Log.trace("{} error disposing {}", getLogLabel(), connection);
			}
		}
		setState(EnumCardState.INVALID);
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

	protected List<CommonCardConnection> getConnections() {
		return new ArrayList<>(connections);
	}

	public String getId() {
		return id;
	}

	protected String getLogLabel() {
		return toString();
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

	protected void removeConnection(CommonCardConnection connection) throws CardException {
		synchronized (lock) {
			connections.remove(connection);
		}
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
		return "card " + id;
	}
}
