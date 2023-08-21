package de.intarsys.security.smartcard.card;

import java.util.ArrayList;
import java.util.List;

import de.intarsys.tools.concurrent.ITaskCallback;
import de.intarsys.tools.concurrent.TaskFailed;
import de.intarsys.tools.exception.ExceptionTools;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * A concrete monitor that delegates connection events to its listeners
 * 
 */
public class CardConnectionMonitor extends CommonCardConnectionMonitor {

	private static final ILogger Log = PACKAGE.Log;

	private final List<ITaskCallback<ICardConnection>> listeners = new ArrayList<>();

	private boolean closeConnection = false;

	public CardConnectionMonitor(ICardSystem cardSystem) {
		super(cardSystem);
	}

	/**
	 * Add an {@link ITaskCallback} to get informed about the outcome of a
	 * connection attempt.
	 * 
	 * The listener should wrap any {@link CardException} it wants to throw
	 * while working on the connection callback in a {@link RuntimeException}.
	 * 
	 * The listeners are executed in sequence of registration. An exception in
	 * any of the listeners will cause the connection to be closed.
	 * 
	 * @param listener
	 */
	public void addCardConnectionListener(ITaskCallback<ICardConnection> listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public boolean isCloseConnection() {
		return closeConnection;
	}

	@Override
	protected void onCardInserted(ICard card) {
		super.onCardInserted(card);
		EnumCardState state = card.getState();
		if (state.isConnectedShared() || state.isNotConnected()) {
			connect(card);
		}
	}

	@Override
	protected void onConnected(ICardConnection connection) throws CardException {
		List<ITaskCallback<ICardConnection>> tempListeners;
		synchronized (listeners) {
			tempListeners = new ArrayList<>(listeners);
		}
		boolean close = isCloseConnection();
		try {
			for (ITaskCallback<ICardConnection> listener : tempListeners) {
				listener.finished(connection);
			}
		} catch (RuntimeException e) {
			close = true;
			CardException nested = ExceptionTools.getFromChain(e, CardException.class);
			if (nested != null) {
				throw nested;
			}
			throw e;
		} finally {
			if (close) {
				try {
					connection.close(ICardConnection.MODE_LEAVE_CARD);
				} catch (CardException e) {
					Log.warn("{} close {} failed", getLogPrefix(), connection); //$NON-NLS-1$
				}
			}
		}
	}

	@Override
	protected void onConnectionFailed(ICard card, TaskFailed exception) {
		List<ITaskCallback<ICardConnection>> tempListeners;
		synchronized (listeners) {
			tempListeners = new ArrayList<>(listeners);
		}
		for (ITaskCallback<ICardConnection> listener : tempListeners) {
			listener.failed(exception);
		}
	}

	public void removeCardConnectionListener(ITaskCallback<ICardConnection> listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public void setCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
	}
}
