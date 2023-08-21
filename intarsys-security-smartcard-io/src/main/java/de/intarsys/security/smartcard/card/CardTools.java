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

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.intarsys.tools.attribute.Attribute;
import de.intarsys.tools.concurrent.AbstractFutureTask;
import de.intarsys.tools.concurrent.ITaskCallback;
import de.intarsys.tools.concurrent.TaskFailed;
import de.intarsys.tools.concurrent.ThreadTools;
import de.intarsys.tools.exception.ExceptionTools;
import de.intarsys.tools.oid.IOIDGenerator;
import de.intarsys.tools.oid.PronouncableOIDGenerator;
import de.intarsys.tools.yalf.api.ILogger;
import de.intarsys.tools.yalf.api.IMDC;
import de.intarsys.tools.yalf.api.Yalf;

/**
 * Tool methods for handling the {@link ICard} subsystem
 * 
 */
public final class CardTools {

	static class ConnectTransactedTask extends AbstractFutureTask<ICardConnection> {

		private Future<ICardConnection> cardConnectTask;

		private Future<Void> beginTransactionTask;

		private ICardConnection connection;

		private final ICard card;

		protected ConnectTransactedTask(ICard card) {
			super();
			this.card = card;
			setAsynch(true);
			created();
		}

		@Override
		public boolean cancel(boolean interrupt) {
			boolean canceled = super.cancel(interrupt);
			synchronized (this) {
				if (beginTransactionTask != null) {
					beginTransactionTask.cancel(false);
				}
				if (cardConnectTask != null) {
					cardConnectTask.cancel(false);
				}
			}
			return canceled;
		}

		@Override
		protected ICardConnection compute() throws CardException {
			synchronized (this) {
				startCardConnectTask();
			}
			return null;
		}

		public ICard getCard() {
			return card;
		}

		private void onConnectFailed(Throwable t) {
			Log.debug("{} {} connect failed ({})", getLabel(), card, ExceptionTools.getMessage(t)); //$NON-NLS-1$
			setException(t);
		}

		private void onConnectSuccess(ICardConnection newCardChannel) {
			synchronized (this) {
				connection = newCardChannel;
			}
			if (isCancelled()) {
				// premature escape...
				setResult(null);
				return;
			}
			try {
				synchronized (this) {
					startBeginTransactionTask();
				}
			} catch (RuntimeException e) {
				setException(new CardException("unexpected exception", e)); //$NON-NLS-1$
			}
		}

		private void onTransactionFailed(Throwable t) {
			Log.debug("{} {} transaction begin failed ({})", getLabel(), card, ExceptionTools.getMessage(t)); //$NON-NLS-1$
			setException(t);
		}

		private void onTransactionSuccess() {
			if (isCancelled()) {
				// premature escape...
				setResult(null);
				return;
			}
			try {
				setResult(connection);
			} catch (RuntimeException e) {
				setException(new CardException("unexpected exception", e)); //$NON-NLS-1$
			}
		}

		private void startBeginTransactionTask() {
			beginTransactionTask = connection.beginTransaction(new ITaskCallback<Void>() {
				@Override
				public void failed(TaskFailed exception) {
					if (!exception.isCancellation()) {
						onTransactionFailed(exception.getCause());
					}
				}

				@Override
				public void finished(Void result) {
					onTransactionSuccess();
				}
			});
		}

		private void startCardConnectTask() {
			cardConnectTask = getCard().connectShared(ICardTerminal.PROTOCOL_Tx, new ITaskCallback<ICardConnection>() {
				@Override
				public void failed(TaskFailed exception) {
					if (!exception.isCancellation()) {
						onConnectFailed(exception.getCause());
					}
				}

				@Override
				public void finished(ICardConnection cardChannel) {
					onConnectSuccess(cardChannel);
				}
			});
		}

		@Override
		protected void taskFailed() {
			synchronized (this) {
				if (connection != null) {
					try {
						connection.close(ICardConnection.MODE_LEAVE_CARD);
					} catch (CardException e) {
						Log.trace("{} {} close failed ({})", getLabel(), connection, ExceptionTools.getMessage(e));
					}
				}

			}
			super.taskFailed();
		}

		@Override
		protected void undo() {
			undoAll();
		}

		private void undoAll() {
			synchronized (this) {
				if (connection != null) {
					try {
						connection.close(ICardConnection.MODE_LEAVE_CARD);
					} catch (CardException e) {
						Log.trace("{} {} close failed ({})", getLabel(), connection, ExceptionTools.getMessage(e));
					}
				}
			}
		}
	}

	private static final IOIDGenerator<String> OID_GENERATOR = new PronouncableOIDGenerator();

	protected static final long RETRY_DELAY = 200;

	// in case of connection failures we support retry attempts
	private static final Attribute ATTR_RetryCount = new Attribute("retryCount");
	private static final Attribute ATTR_RetryDelay = new Attribute("retryDelay");

	protected static final ILogger Log = PACKAGE.Log;

	/**
	 * Open a transaction on an {@link ICardConnection}.
	 * 
	 * @param connection
	 * @param millisecTimeout
	 * @throws CardException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public static void beginTransaction(ICardConnection connection, int millisecTimeout)
			throws CardException, TimeoutException, InterruptedException {
		Future<Void> f = connection.beginTransaction(null);
		try {
			f.get(millisecTimeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			cancel(f, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			cancel(f, e);
		} catch (ExecutionException e) {
			throw CardException.create("begin transaction failed", ExceptionTools.unwrap(e));
		}
	}

	protected static <T> T cancel(Future<T> f, Exception e)
			throws CardException, TimeoutException, InterruptedException {
		if (!f.cancel(false)) {
			try {
				return f.get(-1, TimeUnit.MILLISECONDS);
			} catch (ExecutionException ex) {
				throw CardException.create(e.getCause());
			} catch (Exception ignore) {
				//
			}
		}
		if (e instanceof TimeoutException) {
			throw (TimeoutException) e;
		} else if (e instanceof InterruptedException) {
			throw (InterruptedException) e;
		} else {
			throw CardException.create(ExceptionTools.unwrap(e));
		}
	}

	/**
	 * Create an {@link ICardConnection} .
	 * 
	 * @param card
	 * @param millisecTimeout
	 * @return
	 * @throws CardException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public static ICardConnection connectShared(ICard card, int millisecTimeout)
			throws CardException, TimeoutException, InterruptedException {
		Future<ICardConnection> f = card.connectShared(ICardTerminal.PROTOCOL_Tx, null);
		try {
			return f.get(millisecTimeout, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			return cancel(f, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return cancel(f, e);
		} catch (ExecutionException e) {
			throw CardException.create(e.getCause());
		}
	}

	/**
	 * Create an {@link ICardConnection}.
	 * 
	 * @param card
	 * @param callback
	 * @return
	 */
	public static Future<ICardConnection> connectShared(ICard card, final ITaskCallback<ICardConnection> callback) {
		return card.connectShared(ICardTerminal.PROTOCOL_Tx, callback);
	}

	/**
	 * Create an {@link ICardConnection} and open a transaction in a single
	 * step.
	 * 
	 * @param card
	 * @param millisecTimeout
	 * @return
	 * @throws CardException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public static ICardConnection connectTransacted(ICard card, int millisecTimeout)
			throws CardException, TimeoutException, InterruptedException {
		ICardConnection connection = CardTools.connectShared(card, millisecTimeout);
		try {
			CardTools.beginTransaction(connection, millisecTimeout);
			return connection;
		} catch (TimeoutException e) {
			connection.close(ICardConnection.MODE_LEAVE_CARD);
			throw e;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			connection.close(ICardConnection.MODE_LEAVE_CARD);
			throw e;
		} catch (RuntimeException e) {
			connection.close(ICardConnection.MODE_LEAVE_CARD);
			throw e;
		}
	}

	/**
	 * Create an {@link ICardConnection} and open a transaction in a single
	 * callback.
	 * 
	 * @param card
	 * @param callback
	 * @return
	 */
	public static Future<ICardConnection> connectTransacted(ICard card, final ITaskCallback<ICardConnection> callback) {
		ConnectTransactedTask task = new ConnectTransactedTask(card);
		if (callback != null) {
			task.addTaskCallback(callback);
		}
		// this is an asynchronous computation!
		task.run();
		return task;
	}

	public static ScheduledExecutorService createExecutor(String id) {
		return new ScheduledThreadPoolExecutor(1, ThreadTools.newThreadFactoryDaemon(id)) {
			@Override
			public void execute(Runnable command) {
				Map<String, String> contextMap = Yalf.get().getMDC().getCopyOfContextMap();
				super.execute(() -> {
					IMDC mdc = Yalf.get().getMDC();
					Map<String, String> previousContextMap = mdc.getCopyOfContextMap();
					if (contextMap == null) {
						mdc.clear();
					} else {
						mdc.setContextMap(contextMap);
					}
					try {
						command.run();
					} finally {
						if (previousContextMap == null) {
							mdc.clear();
						} else {
							mdc.setContextMap(previousContextMap);
						}
					}
				});
			}
		};
	}

	public static String createId() {
		return OID_GENERATOR.createOID();
	}

	/**
	 * Factor out common code to handle retry attempts.
	 * 
	 * @param card
	 * @param e
	 * @param maxRetries
	 *            The number of retry attempts (in addition to the initial
	 *            attempt) that should be performed
	 * @return
	 */
	public static boolean isRetry(ICard card, Throwable e, int maxRetries) {
		if (card == null) {
			return false;
		}
		boolean retry = false;
		boolean log = false;
		Integer retryCount = (Integer) card.getAttribute(ATTR_RetryCount);
		if (retryCount == null) {
			retryCount = maxRetries;
		}
		Long retryDelay = (Long) card.getAttribute(ATTR_RetryDelay);
		if (retryDelay == null) {
			retryDelay = RETRY_DELAY;
		}
		if (ExceptionTools.isInChain(e, CardReset.class)) {
			retry = true;
			log = true;
		} else if (ExceptionTools.isInChain(e, CardUnavailable.class)) {
			retry = false;
			log = true;
		} else if (e != null && maxRetries > 0) {
			/*
			 * e.g. CardSharingViolation
			 */
			retryCount--;
			card.setAttribute(ATTR_RetryCount, retryCount);
			retry = retryCount >= 0;
			log = true;
		}
		try {
			if (retry) {
				Thread.sleep(retryDelay);
				retryDelay = retryDelay * 2;
				if (retryDelay > 60000L) {
					retryDelay = 60000L;
				}
				card.setAttribute(ATTR_RetryDelay, retryDelay);
			}
		} catch (InterruptedException ignore) {
			Thread.currentThread().interrupt();
		}
		if (log) {
			Log.debug("{} retry {} ({})", card, retry ? "accept" : "deny", ExceptionTools.getMessage(e));
		}
		return retry;
	}

	/**
	 * Factor out common code to handle reset exceptions.
	 * 
	 * @param card
	 * @param e
	 * @return
	 */
	public static boolean isRetryReset(ICard card, Throwable e) {
		boolean retry = false;
		if (ExceptionTools.isInChain(e, CardReset.class)) {
			retry = true;
		}
		try {
			if (retry) {
				Thread.sleep(RETRY_DELAY);
			}
		} catch (InterruptedException ignore) {
			Thread.currentThread().interrupt();
		}
		return retry;
	}

	/**
	 * Factor out common code to handle retry attempts.
	 * 
	 * @param card
	 */
	public static void resetRetry(ICard card) {
		if (card == null) {
			return;
		}
		card.setAttribute(ATTR_RetryCount, null);
	}

	private CardTools() {
	}

}
