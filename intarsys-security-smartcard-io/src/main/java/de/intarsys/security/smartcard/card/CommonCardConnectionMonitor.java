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

import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import de.intarsys.tools.attribute.Attribute;
import de.intarsys.tools.concurrent.ITaskCallback;
import de.intarsys.tools.concurrent.Promise;
import de.intarsys.tools.concurrent.TaskFailed;
import de.intarsys.tools.exception.ExceptionTools;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * Abstract superclass to ease the implementation of monitors that connect to a
 * card upon insertion.
 * <p>
 * Subclasses must redefine onConnected() to take further actions after initial
 * connection is established. The default will close the connection immediately.
 */
public abstract class CommonCardConnectionMonitor extends CardSystemMonitor {

	private static final ILogger Log = PACKAGE.Log;

	private final Attribute attrConnection = new Attribute("connection");

	private final Attribute attrDelay = new Attribute("delay");

	public CommonCardConnectionMonitor(ICardSystem cardSystem) {
		super(cardSystem);
	}

	/**
	 * Spawn a task to connect to the card. The client is informed about the outcome
	 * of the task via the callback methods "onConnected" or "onConnectionFailed".
	 * These callbacks are performed with the data argument supplied by the original
	 * caller.
	 * 
	 * @param card
	 *            The {@link ICard} to connect to.
	 * @return
	 */
	public Future<ICardConnection> connect(final ICard card) {
		synchronized (attrConnection) {
			Object connectionMarker = card.getAttribute(attrConnection);
			if (connectionMarker != null) {
				if (connectionMarker instanceof ICardConnection) {
					if (((ICardConnection) connectionMarker).isValid()) {
						return Promise.newFailed(new IllegalStateException("already connecting/connected"));
					}
				} else {
					return Promise.newFailed(new IllegalStateException("already connecting/connected"));
				}
			}
			Log.info("{} connect {}...", getLogPrefix(), card); //$NON-NLS-1$
			Future<ICardConnection> connectTask = CardTools.connectTransacted(card,
					new ITaskCallback<ICardConnection>() {
						@Override
						public void failed(TaskFailed exception) {
							synchronized (attrConnection) {
								card.setAttribute(attrConnection, null);
							}
							boolean retry = isStarted() && CardTools.isRetry(card, exception, 2);
							if (retry) {
								Log.info("{} connect {} failed ({}), retry", getLogPrefix(), card,
										ExceptionTools.getMessage(exception));
								connectLater(card);
							} else {
								Log.info("{} connect {} failed", getLogPrefix(), card, exception);
								onConnectionFailed(card, exception);
							}
						}

						@Override
						public void finished(ICardConnection connection) {
							synchronized (attrConnection) {
								card.setAttribute(attrConnection, connection);
							}
							Log.info("{} connect {} success", getLogPrefix(), connection); //$NON-NLS-1$
							CardTools.resetRetry(card);
							if (!isStarted()) {
								doClose(connection);
								return;
							}
							/*
							 * get out of connection thread
							 */
							getEventExecutor().submit(new Runnable() {
								@Override
								public void run() {
									if (isStarted()) {
										try {
											onConnected(connection);
											card.setAttribute(attrDelay, null);
											return;
										} catch (CardReset e) {
											Log.info("{} connect {} reset, retry", getLogPrefix(), connection);
											connectLater(card);
										} catch (CardUnavailable e) {
											Log.info("{} connect {} removed", getLogPrefix(), connection);
										} catch (Exception e) {
											Log.warn("{} connect {} exception", getLogPrefix(), connection, e);
										}
										onConnectedGiveup(connection);
									}
									doClose(connection);
								}
							});
						}
					});
			card.setAttribute(attrConnection, connectTask);
			return connectTask;
		}
	}

	public void connectLater(ICard card) {
		if (!isStarted()) {
			return;
		}
		Long delay = (Long) card.getAttribute(attrDelay);
		if (delay == null) {
			delay = 1000L + new Random().nextInt(1000);
		} else if (delay > 20000L) {
			//
		} else {
			delay = delay * 2;
		}
		card.setAttribute(attrDelay, delay);
		Log.info("{} schedule {} for re-connection in {} ms", this, card, delay);
		getEventExecutor().schedule(new Runnable() {
			@Override
			public void run() {
				if (!card.getState().isInvalid()) {
					connect(card);
				}
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	protected void doClose(ICardConnection connection) {
		try {
			connection.close(ICardConnection.MODE_LEAVE_CARD);
		} catch (CardException e) {
			Log.trace("{} connect {} close unexpected exception", getLogPrefix(), //$NON-NLS-1$
					connection, ExceptionTools.getMessage(e));
		}
	}

	protected void onConnected(ICardConnection connection) throws CardException {
		doClose(connection);
	}

	protected void onConnectedGiveup(ICardConnection connection) {
	}

	protected void onConnectionFailed(ICard card, TaskFailed exception) {
	}
}
