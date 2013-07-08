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

import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import de.intarsys.tools.concurrent.AbstractFutureTask;

/**
 * Tool methods for handling the {@link ICard} subsystem
 * 
 */
public class CardTools {

	static class ConnectTransactedTask extends
			AbstractFutureTask<ICardConnection> {

		private final IConnectionCallback callback;

		private Future<ICardConnection> cardConnectTask;

		private Future<Void> beginTransactionTask;

		private ICardConnection connection;

		private final ICard card;

		protected ConnectTransactedTask(ICard card, IConnectionCallback callback) {
			this.card = card;
			this.callback = callback;
			setAsynch(true);
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

		private void onConnectFailed(CardException cardException) {
			if (Log.isLoggable(Level.FINEST)) {
				Log.log(Level.FINEST, "" + this + " connect failed"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			setException(cardException);
		}

		private void onConnectSuccess(ICardConnection newCardChannel) {
			if (Log.isLoggable(Level.FINEST)) {
				Log.log(Level.FINEST, "" + this + " connect success"); //$NON-NLS-1$ //$NON-NLS-2$
			}
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

		private void onTransactionFailed(CardException cardException) {
			if (Log.isLoggable(Level.FINEST)) {
				Log.log(Level.FINEST, "" + this + " transaction begin failed"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			setException(cardException);
		}

		private void onTransactionSuccess() {
			if (Log.isLoggable(Level.FINEST)) {
				Log.log(Level.FINEST, "" + this + " transaction begin success"); //$NON-NLS-1$ //$NON-NLS-2$
			}
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
			beginTransactionTask = connection
					.beginTransaction(new ITransactionCallback() {
						@Override
						public void transactionCreated() {
							onTransactionSuccess();
						}

						@Override
						public void transactionFailed(
								CardException cardException) {
							onTransactionFailed(cardException);
						}
					});
		}

		private void startCardConnectTask() {
			cardConnectTask = getCard()
					.connectShared(
							new de.intarsys.security.smartcard.card.IConnectionCallback() {
								@Override
								public void connected(
										ICardConnection cardChannel) {
									onConnectSuccess(cardChannel);
								}

								@Override
								public void connectionFailed(
										CardException cardException) {
									onConnectFailed(cardException);
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
						Log.log(Level.FINE, "connection close failed", e);
					}
				}

			}
			if (callback != null) {
				CardException tempEx;
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
			undoAll();
		}

		private void undoAll() {
			synchronized (this) {
				if (connection != null) {
					try {
						connection.close(ICardConnection.MODE_LEAVE_CARD);
					} catch (CardException e) {
						Log.log(Level.FINE, "connection close failed", e);
					}
				}
			}
		}
	}

	/**
	 * Create an {@link ICardConnection}.
	 * 
	 * @param card
	 * @param callback
	 * @return
	 */
	static public Future<ICardConnection> connectShared(ICard card,
			final IConnectionCallback callback) {
		return card.connectShared(callback);
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
	static public ICardConnection connectShared(ICard card, int millisecTimeout)
			throws CardException, TimeoutException, InterruptedException {
		return card.connectShared(millisecTimeout);
	}

	/**
	 * Create an {@link ICardConnection} and open a transaction in a single
	 * callback.
	 * 
	 * @param card
	 * @param callback
	 * @return
	 */
	static public Future<ICardConnection> connectTransacted(ICard card,
			final IConnectionCallback callback) {
		ConnectTransactedTask task = new ConnectTransactedTask(card, callback);
		// this is an asynchronous computation!
		task.run();
		return task;
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
	static public ICardConnection connectTransacted(ICard card,
			int millisecTimeout) throws CardException, TimeoutException,
			InterruptedException {
		ICardConnection cardChannel = card.connectShared(millisecTimeout);
		try {
			cardChannel.beginTransaction(millisecTimeout);
			return cardChannel;
		} catch (CardException e) {
			cardChannel.close(ICardConnection.MODE_LEAVE_CARD);
			throw e;
		} catch (TimeoutException e) {
			cardChannel.close(ICardConnection.MODE_LEAVE_CARD);
			throw e;
		} catch (InterruptedException e) {
			cardChannel.close(ICardConnection.MODE_LEAVE_CARD);
			throw e;
		} catch (RuntimeException e) {
			cardChannel.close(ICardConnection.MODE_LEAVE_CARD);
			throw e;
		}
	}

}
