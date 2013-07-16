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

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.tools.attribute.AttributeMap;
import de.intarsys.tools.concurrent.AbstractFutureTask;

/**
 * A common implementation for {@link ICardConnection}.
 * 
 * This implementation provides the skeleton for real card acccess. Most
 * important, it relies on an {@link Executor} to move card access in a thread
 * of its own. This executor is used at least for connection, transaction and
 * close.
 * 
 */
abstract public class CommonCardConnection implements ICardConnection {

	public class BeginTransactionTask extends AbstractFutureTask<Void> {

		private ITransactionCallback callback;

		public BeginTransactionTask(ITransactionCallback callback) {
			this.callback = callback;
		}

		@Override
		protected Void compute() throws Exception {
			synchronized (lock) {
				checkValidity();
				if (transactionActive) {
					throw new CardException("transaction already requested");
				}
				transactionActive = true;
			}
			if (Log.isLoggable(Level.FINEST)) {
				Log.finest(getLabel() + " beginTransaction"); //$NON-NLS-1$ 
			}
			basicBeginTransaction();
			markBeginTransaction();
			return null;
		}

		@Override
		protected void taskFailed() {
			synchronized (lock) {
				transactionActive = false;
			}
			if (callback != null) {
				final CardException tempEx;
				if (basicGetException() instanceof CardException) {
					tempEx = (CardException) basicGetException();
				} else {
					tempEx = new CardException("unexpected exception", //$NON-NLS-1$
							basicGetException());
				}
				callback.transactionFailed(tempEx);
			}
		}

		@Override
		protected void taskFinished() {
			if (callback != null) {
				callback.transactionCreated();
			}
		}

		@Override
		protected void undo() {
			try {
				endTransaction();
			} catch (CardException e) {
				Log.log(Level.FINE, getLabel()
						+ " undo for end transaction failed " + this);
			}
		}
	}

	public class CloseTask extends AbstractFutureTask<Void> {

		final private ICloseCallback callback;

		final private int mode;

		public CloseTask(int mode, ICloseCallback callback) {
			this.mode = mode;
			this.callback = callback;
		}

		@Override
		protected Void compute() throws Exception {
			synchronized (lock) {
				if (closed) {
					return null;
				}
				closed = true;
			}
			if (Log.isLoggable(Level.FINEST)) {
				Log.finest(getLabel() + " close"); //$NON-NLS-1$ 
			}
			basicClose(mode);
			return null;
		}

		@Override
		protected void taskFailed() {
			if (callback != null) {
				final CardException tempEx;
				if (basicGetException() instanceof CardException) {
					tempEx = (CardException) basicGetException();
				} else {
					tempEx = new CardException("unexpected exception", //$NON-NLS-1$
							basicGetException());
				}
				callback.closeFailed(tempEx);
			}
		}

		@Override
		protected void taskFinally() {
			super.taskFinally();
			basicCloseFinally(mode);
		}

		@Override
		protected void taskFinished() {
			if (callback != null) {
				callback.closed();
			}
		}

	}

	private static int Counter = 0;

	/**
	 * On windows (only?) a PCSC transaction will time out after 5 seconds.
	 */
	protected static final int PCSC_TRANSACTION_TIMEOUT = 4000;

	public static synchronized int createId() {
		return Counter++;
	}

	final private AttributeMap attributes = new AttributeMap();

	private boolean transactionActive = false;

	private long touched;

	final private int id;

	final private ScheduledExecutorService executor;

	// used in inner class
	protected boolean closed = false;

	final private CommonCard card;

	final private boolean exclusive;

	final private CommonCardTerminal cardTerminal;

	final protected Object lock = new Object();

	private static final Logger Log = PACKAGE.Log;

	final private Runnable runKeepAlive = new Runnable() {
		@Override
		public void run() {
			keepAlive();
		}
	};

	protected CommonCardConnection(CommonCard card, int id,
			ScheduledExecutorService executorTask, boolean exclusive) {
		super();
		this.card = card;
		this.id = id;
		this.cardTerminal = card.basicGetCardTerminal();
		this.executor = executorTask;
		this.exclusive = exclusive;
	}

	protected CommonCardConnection(CommonCardTerminal cardTerminal, int id,
			ScheduledExecutorService executorTask, boolean exclusive) {
		super();
		this.card = null;
		this.id = id;
		this.cardTerminal = cardTerminal;
		this.executor = executorTask;
		this.exclusive = exclusive;
	}

	abstract protected void basicBeginTransaction() throws CardException;

	abstract protected void basicClose(int mode) throws CardException;

	protected void basicCloseFinally(int mode) {
		executor.shutdown();
	}

	abstract protected byte[] basicControl(int controlCode, byte[] inBuffer,
			int inBufferOffset, int inBufferLength, int outBufferSize)
			throws CardException;

	abstract protected byte[] basicControlMapped(int controlCode,
			byte[] inBuffer, int inBufferOffset, int inBufferLength,
			int outBufferSize) throws CardException;

	abstract protected void basicEndTransaction() throws CardException;

	abstract protected byte[] basicGetAttrib(int attribId) throws CardException;

	protected CommonCard basicGetCard() {
		return card;
	}

	protected CommonCardTerminal basicGetCardTerminal() {
		return cardTerminal;
	}

	protected void basicGetStatus() throws CardException {
	}

	abstract protected void basicReconnect(int mode) throws CardException;

	abstract protected ResponseAPDU basicTransmit(RequestAPDU request)
			throws CardException;

	@Override
	public Future<Void> beginTransaction(final ITransactionCallback callback) {
		if (getCard() == null) {
			// direct connection
			throw new IllegalStateException("direct connection"); //$NON-NLS-1$
		}
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest(getLabel() + " request beginTransaction"); //$NON-NLS-1$ 
		}
		BeginTransactionTask transactionTask = new BeginTransactionTask(
				callback);
		execute(transactionTask);
		return transactionTask;
	}

	protected void cancelTransactionTask(Future<Void> transactionTask) {
		if (!transactionTask.cancel(false)) {
			try {
				endTransaction();
			} catch (Exception e) {
				//
			}
		}
	}

	protected void checkValidity() throws CardInvalidException {
		if (!isValid()) {
			throw new CardInvalidException();
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
			// queuing is required to be synchronized
			// may access a shutdown queue otherwise
			CloseTask task = new CloseTask(mode, null);
			execute(task);
			// experimental state....
			// return task;
		}
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest(getLabel() + " request close"); //$NON-NLS-1$ 
		}
	}

	@Override
	public byte[] control(int controlCode, byte[] inBuffer, int inBufferOffset,
			int inBufferLength, int outBufferSize) throws CardException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest(getLabel()
					+ " control 0x" + Integer.toHexString(controlCode)); //$NON-NLS-1$ 
		}
		synchronized (lock) {
			checkValidity();
		}
		return basicControl(controlCode, inBuffer, inBufferOffset,
				inBufferLength, outBufferSize);
	}

	@Override
	public byte[] controlMapped(int controlCode, byte[] inBuffer,
			int inBufferOffset, int inBufferLength, int outBufferSize)
			throws CardException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest(getLabel()
					+ " control mapped 0x" + Integer.toHexString(controlCode)); //$NON-NLS-1$ 
		}
		synchronized (lock) {
			checkValidity();
		}
		return basicControlMapped(controlCode, inBuffer, inBufferOffset,
				inBufferLength, outBufferSize);
	}

	@Override
	public void endTransaction() throws CardException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest(getLabel() + " end transaction"); //$NON-NLS-1$ 
		}
		synchronized (lock) {
			checkValidity();
			if (!transactionActive) {
				return;
			}
		}
		try {
			basicEndTransaction();
		} finally {
			synchronized (lock) {
				transactionActive = false;
			}
		}
	}

	protected void execute(Runnable task) {
		executor.execute(task);
	}

	@Override
	public byte[] getAttrib(int attribId) throws CardException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest(getLabel() + " get attrib " + attribId); //$NON-NLS-1$ 
		}
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

	public int getId() {
		return id;
	}

	protected String getLabel() {
		return "" + this;
	}

	@Override
	public void getStatus() throws CardException {
		synchronized (lock) {
			checkValidity();
		}
		basicGetStatus();
	}

	protected long getTouched() {
		return touched;
	}

	@Override
	public boolean isTransactionActive() {
		synchronized (lock) {
			return transactionActive;
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
			if (!isTransactionActive() || !isValid()) {
				return;
			}
			long now = System.currentTimeMillis();
			long idle = now - getTouched();
			if (idle >= PCSC_TRANSACTION_TIMEOUT - 100) {
				idle = 0;
				getStatus();
				Log.log(Level.FINEST, getLabel()
						+ " keep PSCS transaction alive success");
			}
			executor.schedule(runKeepAlive, PCSC_TRANSACTION_TIMEOUT - idle,
					TimeUnit.MILLISECONDS);
		} catch (CardException e) {
			Log.log(Level.FINE,
					getLabel() + " keep PSCS transaction alive failed ("
							+ e.getLocalizedMessage() + "'");
		}
	}

	protected void markBeginTransaction() {
		synchronized (lock) {
			transactionActive = true;
		}
		markTouched();
		card.fromConnectionBeginTransaction(CommonCardConnection.this);
		executor.schedule(runKeepAlive, PCSC_TRANSACTION_TIMEOUT,
				TimeUnit.MILLISECONDS);
	}

	/**
	 * Remember when the last command to PCSC was issued. This is important as
	 * since Windows 8 connections with active transactions are reset after more
	 * than 5 sec idle time. Now we simply try to keep alive.
	 */
	protected void markTouched() {
		this.touched = System.currentTimeMillis();
	}

	@Override
	public void reconnect(int mode) throws CardException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest(getLabel() + " reconnect connection"); //$NON-NLS-1$ 
		}
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
		return "connection " + id + " on " + card; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public ResponseAPDU transmit(RequestAPDU request) throws CardException {
		synchronized (lock) {
			checkValidity();
		}
		markTouched();
		return basicTransmit(request);
	}

}
