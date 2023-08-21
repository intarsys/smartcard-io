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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.intarsys.security.smartcard.pcsc.PCSCAttribute;
import de.intarsys.tools.attribute.AttributeMap;
import de.intarsys.tools.concurrent.AbstractFutureTask;
import de.intarsys.tools.concurrent.ITaskCallback;
import de.intarsys.tools.exception.ExceptionTools;
import de.intarsys.tools.reflect.ClassTools;
import de.intarsys.tools.system.SystemTools;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * A common implementation for {@link ICardConnection}.
 * 
 * This implementation provides the skeleton for real card acccess. Most
 * important, it relies on an {@link Executor} to move card access in a thread
 * of its own. This executor is used at least for connection, transaction and
 * close.
 * 
 */
public abstract class CommonCardConnection implements ICardConnection {

	public class BeginTransactionTask extends AbstractFutureTask<Void> {

		public BeginTransactionTask() {
			super();
			created();
		}

		@Override
		protected Void compute() throws Exception {
			checkValidity();
			synchronized (lock) {
				transactionActive = true;
			}
			basicBeginTransaction();
			return null;
		}

		@Override
		protected void taskFailed() {
			synchronized (lock) {
				transactionActive = false;
			}
			super.taskFailed();
		}

		@Override
		protected void undo() {
			try {
				endTransaction();
			} catch (CardException e) {
				Log.trace("{} {} undo for end transaction failed", getLabel(), CommonCardConnection.this);
			}
		}
	}

	public class CloseTask extends AbstractFutureTask<Void> {

		private final int mode;

		public CloseTask(int mode) {
			super();
			this.mode = mode;
			created();
		}

		@Override
		protected Void compute() throws Exception {
			synchronized (lock) {
				if (closed) {
					return null;
				}
				transactionActive = false;
				closed = true;
			}
			// be aware of direct connections
			if (basicGetCard() != null) {
				basicGetCard().removeConnection(CommonCardConnection.this);
			}
			basicClose(mode);
			return null;
		}

		@Override
		protected void taskFinally() {
			super.taskFinally();
			basicCloseFinally(mode);
		}

	}

	public static boolean DEBUG = SystemTools.isDebug("pcsc");

	/*
	 * On Windows a PCSC transaction will time out after 5 seconds.
	 * 
	 * We use a much higher frequency here, as Java is not real time. High load or garbage collect
	 * can cause the keep alive to fail!!
	 * 
	 */
	protected static final int PCSC_TRANSACTION_TIMEOUT = 1000;

	private static final ILogger Log = PACKAGE.Log;

	private final AttributeMap attributes = new AttributeMap();

	private boolean transactionActive = false;

	private long touched;

	private int keepAliveCount = 0;

	private final String id;

	private final ScheduledExecutorService executor;

	private final ScheduledExecutorService keepAliveExecutor;

	// used in inner class
	protected boolean closed = false;

	private final CommonCard card;

	private final boolean exclusive;

	private final CommonCardTerminal cardTerminal;

	protected final Object lock = new Object();

	private final Runnable runKeepAlive = new Runnable() {
		@Override
		public void run() {
			keepAlive();
		}
	};

	protected CommonCardConnection(CommonCard card, String suffix, ScheduledExecutorService executorTask,
			boolean exclusive) {
		super();
		this.card = card;
		this.id = card.getId() + "-" + suffix;
		this.cardTerminal = card.basicGetCardTerminal();
		this.executor = executorTask;
		if (DEBUG) {
			this.keepAliveExecutor = new ScheduledThreadPoolExecutor(1);
		} else {
			this.keepAliveExecutor = executor;
		}
		this.exclusive = exclusive;
	}

	protected CommonCardConnection(CommonCardTerminal cardTerminal, String suffix,
			ScheduledExecutorService executorTask,
			boolean exclusive) {
		super();
		this.card = null;
		this.cardTerminal = cardTerminal;
		this.id = cardTerminal.getId() + "-*-" + suffix;
		this.executor = executorTask;
		if (DEBUG) {
			this.keepAliveExecutor = new ScheduledThreadPoolExecutor(1);
		} else {
			this.keepAliveExecutor = executor;
		}
		this.exclusive = exclusive;
	}

	protected void basicBeginTransaction() throws CardException {
		synchronized (lock) {
			markTouched();
		}
		/*
		 * from MSDN: If a transaction is held on the card for more than five
		 * seconds with no operations happening on that card, then the card is
		 * reset. Calling any of the Smart Card and Reader Access Functions or
		 * Direct Card Access Functions on the card that is transacted results
		 * in the timer being reset to continue allowing the transaction to be
		 * used.
		 */
		keepAlive();
	}

	protected abstract void basicClose(int mode) throws CardException;

	protected void basicCloseFinally(int mode) {
		synchronized (lock) {
			executor.shutdownNow();
		}
	}

	protected abstract byte[] basicControl(int controlCode, byte[] inBuffer, int inBufferOffset, int inBufferLength,
			int outBufferSize) throws CardException;

	protected abstract byte[] basicControlMapped(int controlCode, byte[] inBuffer, int inBufferOffset,
			int inBufferLength, int outBufferSize) throws CardException;

	protected abstract void basicEndTransaction() throws CardException;

	protected abstract byte[] basicGetAttrib(int attribId) throws CardException;

	protected CommonCard basicGetCard() {
		return card;
	}

	protected CommonCardTerminal basicGetCardTerminal() {
		return cardTerminal;
	}

	protected void basicGetStatus() throws CardException {
	}

	protected abstract void basicReconnect(int mode) throws CardException;

	protected abstract ResponseAPDU basicTransmit(RequestAPDU request) throws CardException;

	@Override
	public Future<Void> beginTransaction(final ITaskCallback<Void> callback) {
		if (getCard() == null) {
			// direct connection
			CompletableFuture f = new CompletableFuture();
			f.completeExceptionally(new IllegalStateException("direct connection")); //$NON-NLS-1$
			return f;
		}
		Log.trace("{} request beginTransaction", getLogLabel()); //$NON-NLS-1$
		BeginTransactionTask transactionTask = new BeginTransactionTask();
		if (callback != null) {
			transactionTask.addTaskCallback(callback);
		}
		execute(transactionTask);
		return transactionTask;
	}

	protected void checkValidity() throws CardException {
		synchronized (lock) {
			if (basicGetCard() != null) {
				basicGetCard().checkValidity();
			} else if (basicGetCardTerminal() != null) {
				basicGetCardTerminal().checkValidity();
			}
			if (closed) {
				throw new CardReset();
			}
		}
	}

	public void close() throws CardException {
		close(ICardConnection.MODE_LEAVE_CARD);
	}

	@Override
	public void close(int mode) throws CardException {
		synchronized (lock) {
			if (closed) {
				return;
			}
			if (DEBUG) {
				keepAliveExecutor.shutdownNow();
			}
			// queuing is required to be synchronized
			// may access a shutdown queue otherwise
			CloseTask task = new CloseTask(mode);
			execute(task);
			// experimental state....
			// return task;
		}
		Log.trace("{} request close {}", getLogLabel(), mode); //$NON-NLS-1$
	}

	@Override
	public byte[] control(int controlCode, byte[] inBuffer, int inBufferOffset, int inBufferLength, int outBufferSize)
			throws CardException {
		Log.trace("{} control 0x{}", getLogLabel(), Integer.toHexString(controlCode)); //$NON-NLS-1$
		synchronized (lock) {
			checkValidity();
		}
		return basicControl(controlCode, inBuffer, inBufferOffset, inBufferLength, outBufferSize);
	}

	@Override
	public byte[] controlMapped(int controlCode, byte[] inBuffer, int inBufferOffset, int inBufferLength,
			int outBufferSize) throws CardException {
		Log.trace("{} control mapped 0x{}", getLogLabel(), Integer.toHexString(controlCode)); //$NON-NLS-1$
		synchronized (lock) {
			checkValidity();
		}
		return basicControlMapped(controlCode, inBuffer, inBufferOffset, inBufferLength, outBufferSize);
	}

	@Override
	public void endTransaction() throws CardException {
		Log.trace("{} end transaction", getLogLabel()); //$NON-NLS-1$
		synchronized (lock) {
			if (!transactionActive) {
				return;
			}
		}
		try {
			checkValidity();
			basicEndTransaction();
		} finally {
			synchronized (lock) {
				transactionActive = false;
			}
		}
	}

	protected void execute(Runnable task) {
		synchronized (lock) {
			try {
				checkValidity();
				executor.execute(task);
			} catch (CardException e) {
				/*
				 * we want to ensure that task comes to the correct result even when already closed.
				 * the exception will be re-generated within the task execution anyway. Better approach
				 * would have been to "fail" the task right away - but this is not part of the API yet.
				 * 
				 * this happens e.g. when connection is just closed when starting a transaction.
				 */
				task.run();
			}
		}
	}

	@Override
	public byte[] getAttrib(int attribId) throws CardException {
		Log.trace("{} get attrib {} ({})", getLogLabel(), ClassTools.getConstantName(PCSCAttribute.class, attribId),
				attribId);
		synchronized (lock) {
			checkValidity();
		}
		return basicGetAttrib(attribId);
	}

	@Override
	public Object getAttribute(Object key) {
		return attributes.getAttribute(key);
	}

	@Override
	public ICard getCard() {
		return card;
	}

	@Override
	public ICardTerminal getCardTerminal() {
		return cardTerminal;
	}

	public String getId() {
		return id;
	}

	protected String getLogLabel() {
		return "connection " + id;
	}

	@Override
	public void getStatus() throws CardException {
		synchronized (lock) {
			checkValidity();
		}
		basicGetStatus();
	}

	private long getTouched() {
		return touched;
	}

	@Override
	public boolean isTransactionActive() {
		synchronized (lock) {
			return !closed && transactionActive;
		}
	}

	@Override
	public boolean isValid() {
		synchronized (lock) {
			if (closed) {
				return false;
			}
			if (getCard() != null && getCard().getState().isInvalid()) {
				return false;
			}
			if (getCardTerminal() != null && getCardTerminal().isDisposed()) {
				return false;
			}
			return true;
		}
	}

	protected void keepAlive() {
		try {
			long idle = 0;
			synchronized (lock) {
				if (!isTransactionActive()) {
					return;
				}
				long now = System.currentTimeMillis();
				idle = now - getTouched();
			}
			if (idle >= PCSC_TRANSACTION_TIMEOUT) {
				idle = 0;
				keepAliveCount++;
				getStatus();
			}
			keepAliveExecutor.schedule(runKeepAlive, PCSC_TRANSACTION_TIMEOUT - idle, TimeUnit.MILLISECONDS);
		} catch (CardException e) {
			Log.severe("{} PSCS transaction alive check no. {} failed ({})", getLogLabel(), keepAliveCount,
					ExceptionTools.getMessage(e));
			try {
				close();
			} catch (CardException e1) {
				//
			}
		}
	}

	/**
	 * Remember when the last command to PCSC was issued. This is important as
	 * since Windows 8 connections with active transactions are reset after more
	 * than 5 sec idle time. Now we simply try to keep alive.
	 */
	private void markTouched() {
		this.keepAliveCount = 0;
		this.touched = System.currentTimeMillis();
	}

	@Override
	public void reconnect(int mode) throws CardException {
		Log.trace("{} reconnect connection", getLogLabel()); //$NON-NLS-1$
		synchronized (lock) {
			checkValidity();
		}
		basicReconnect(mode);
	}

	@Override
	public Object removeAttribute(Object key) {
		return attributes.removeAttribute(key);
	}

	@Override
	public Object setAttribute(Object key, Object value) {
		return attributes.setAttribute(key, value);
	}

	@Override
	public String toString() {
		return getLogLabel();
	}

	@Override
	public ResponseAPDU transmit(RequestAPDU request) throws CardException {
		synchronized (lock) {
			checkValidity();
			markTouched();
		}
		return basicTransmit(request);
	}

}
