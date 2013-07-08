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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract superclass to ease the implementation of monitors that connect to a
 * card upon insertion.
 * <p>
 * Subclasses must redefine onConnected() to take further actions after initial
 * connection is established. The default will close the connection immediately.
 */
abstract public class CardConnectionMonitor extends CardSystemMonitor {

	private static final Logger Log = PACKAGE.Log;

	private final List<ICard> ignoredCards;

	private final Map<ICard, Future<ICardConnection>> runningConnectTasks;

	public CardConnectionMonitor(ICardSystem cardSystem) {
		super(cardSystem);
		runningConnectTasks = new HashMap<ICard, Future<ICardConnection>>();
		ignoredCards = new ArrayList<ICard>(3);
	}

	public void addIgnoredCard(ICard card) {
		synchronized (lock) {
			ignoredCards.add(card);
		}
	}

	/**
	 * Spawn a task to connect to the card. The client is informed about the
	 * outcome of the task via the callback methods "onConnected" or
	 * "onConnectionFailed". These callbacks are performed with the data
	 * argument supplied by the original caller.
	 * 
	 * @param card
	 *            The {@link ICard} to connect to.
	 * @param data
	 *            The transparent callback data returned to the client upon
	 *            callback
	 * @return
	 */
	protected Future<ICardConnection> connect(final ICard card,
			final Object data) {
		if (Log.isLoggable(Level.FINE)) {
			Log.fine("" + this + " " + card + " connect"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		synchronized (lock) {
			Future<ICardConnection> connectTask = runningConnectTasks.get(card);
			if (connectTask != null) {
				return connectTask;
			}
			connectTask = CardTools.connectTransacted(card,
					new IConnectionCallback() {
						@Override
						public void connected(ICardConnection connection) {
							if (Log.isLoggable(Level.FINE)) {
								Log.fine("" + this + " created " + connection); //$NON-NLS-1$ //$NON-NLS-2$ 
							}
							try {
								onConnected(connection, data);
							} catch (RuntimeException e) {
								Log.log(Level.WARNING,
										""		+ this + " connect callback unexpected exception", e); //$NON-NLS-1$ //$NON-NLS-2$ 
								try {
									connection
											.close(ICardConnection.MODE_RESET);
								} catch (CardException e1) {
									Log.log(Level.WARNING,
											""		+ this + " connect callback connection close failed", e); //$NON-NLS-1$ //$NON-NLS-2$ 
								}
								throw e;
							} finally {
								removeRunningConnectTask(card);
							}
						}

						@Override
						public void connectionFailed(CardException cardException) {
							if (Log.isLoggable(Level.FINE)) {
								Log.fine("" + this + " " + card + " connect failed");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							}
							try {
								onConnectionFailed(card, data, cardException);
							} finally {
								removeRunningConnectTask(card);
							}
						}
					});
			runningConnectTasks.put(card, connectTask);
			return connectTask;
		}
	}

	protected void handleCardChange(ICard card) {
		//
	}

	protected boolean isConnectTaskRunning(ICard card) {
		synchronized (lock) {
			return runningConnectTasks.containsKey(card);
		}
	}

	public boolean isIgnoredCard(ICard card) {
		synchronized (lock) {
			return ignoredCards.contains(card);
		}
	}

	@Override
	protected void onCardChanged(ICard card) {
		if (isIgnoredCard(card)) {
			return;
		}
		if (isConnectTaskRunning(card)) {
			return;
		}
		super.onCardChanged(card);
		handleCardChange(card);
	}

	@Override
	protected void onCardRemoved(ICard card) {
		removeIgnoredCard(card);
		super.onCardRemoved(card);
	}

	protected void onConnected(ICardConnection connection, Object data) {
		try {
			connection.close(ICardConnection.MODE_LEAVE_CARD);
		} catch (CardException e) {
			Log.log(Level.WARNING, "connection close failed");
		}
	}

	protected void onConnectionFailed(ICard card, Object data,
			CardException cardException) {
		//
	}

	public void removeIgnoredCard(ICard card) {
		synchronized (lock) {
			ignoredCards.remove(card);
		}
	}

	protected void removeRunningConnectTask(ICard card) {
		synchronized (lock) {
			runningConnectTasks.remove(card);
		}
	}

	@Override
	public void stop() {
		super.stop();
		synchronized (lock) {
			stopRunningConnectTasks();
			ignoredCards.clear();
		}
	}

	private void stopRunningConnectTasks() {
		List<Future<ICardConnection>> tempTasks = new ArrayList<Future<ICardConnection>>(
				runningConnectTasks.values());
		for (Future<ICardConnection> connectTask : tempTasks) {
			if (!connectTask.cancel(false)) {
				try {
					ICardConnection connection = connectTask.get(0,
							TimeUnit.MILLISECONDS);
					if (connection != null) {
						connection.close(ICardConnection.MODE_LEAVE_CARD);
					}
				} catch (Exception e) {
					//
				}
			}
		}
		runningConnectTasks.clear();
	}

}
