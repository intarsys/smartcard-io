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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.tools.collection.ListTools;
import de.intarsys.tools.string.StringTools;

/**
 * Connect to all available terminals and return the {@link ICardConnection}
 * that match the given filter.
 * 
 * @param <R>
 */
public class CardConnectionDetector<R extends CardConnectionDetector.Result> {

	public static class Result {

		private final ICardConnection cardChannel;

		public Result(ICardConnection connection) {
			this.cardChannel = connection;
		}

		public ICardConnection getCardConnection() {
			return cardChannel;
		}

	}

	private final static Logger Log = PACKAGE.Log;

	private List<ICardTerminal> cardTerminals;

	private ICardConnectionFilter cardChannelFilter;

	private int connectionTimeout = 30000;

	private boolean closeConnection = true;

	public CardConnectionDetector() {
	}

	public void addCardTerminal(ICardTerminal terminal) {
		if (cardTerminals == null) {
			cardTerminals = new ArrayList<ICardTerminal>(3);
		}
		cardTerminals.add(terminal);
	}

	public void addCardTerminal(String name) {
		ICardTerminal terminal = CardSystem.get().getCardTerminal(name);
		if (terminal != null) {
			addCardTerminal(terminal);
		}
	}

	public List<R> findAll() {
		return findForTerminals(false);
	}

	public R findFirst() {
		List<R> result = findForTerminals(true);
		if (result == null || result.size() == 0) {
			return null;
		} else {
			return result.get(0);
		}
	}

	protected List<R> findForConnection(ICardConnection connection) {
		return ListTools.with((R) new Result(connection));
	}

	protected List<R> findForTerminal(ICardTerminal terminal) {
		ICardConnection connection = null;
		try {
			ICard card = terminal.getCard();
			connection = CardTools.connectTransacted(card,
					getConnectionTimeout());
			List<R> result = null;
			if (getCardChannelFilter() == null) {
				result = findForConnection(connection);
			} else {
				if (getCardChannelFilter().accept(connection)) {
					result = findForConnection(connection);
				}
			}
			if (result == null || result.isEmpty() || isCloseConnection()) {
				connection.close(ICardConnection.MODE_LEAVE_CARD);
			}
			return result;
		} catch (Exception ignore) {
			Log.log(Level.FINEST, ignore.getLocalizedMessage(), ignore);
			if (connection != null) {
				try {
					connection.close(ICardConnection.MODE_RESET);
				} catch (CardException e) {
					Log.log(Level.FINE, "close failed");
				}
			}
			return null;
		}
	}

	protected List<R> findForTerminals(boolean returnFirst) {
		List<ICardTerminal> searchTerminals = cardTerminals;
		if (searchTerminals == null || searchTerminals.isEmpty()) {
			ICardSystem cardSystem = CardSystem.get();
			ICardTerminal[] terminals = cardSystem.getCardTerminals();
			searchTerminals = Arrays.asList(terminals);
		}
		return findForTerminals(searchTerminals, returnFirst);
	}

	protected List<R> findForTerminals(List<ICardTerminal> terminals,
			boolean returnFirst) {
		List<R> allResults = new ArrayList<R>(terminals.size());
		for (ICardTerminal terminal : terminals) {
			if (terminal.getCard() == null) {
				continue;
			}
			List<R> result = findForTerminal(terminal);
			if (result != null) {
				allResults.addAll(result);
				if (returnFirst) {
					break;
				}
			}
		}
		return allResults;
	}

	public ICardConnectionFilter getCardChannelFilter() {
		return cardChannelFilter;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public boolean isCloseConnection() {
		return closeConnection;
	}

	public void removeCardTerminal(ICardTerminal removeTerminal) {
		if (removeTerminal == null || cardTerminals == null) {
			return;
		}
		cardTerminals.remove(removeTerminal);
	}

	public void removeCardTerminalName(String removeTerminalName) {
		if (StringTools.isEmpty(removeTerminalName) || cardTerminals == null) {
			return;
		}
		for (ICardTerminal terminal : cardTerminals) {
			if (terminal.getName().equals(removeTerminalName)) {
				cardTerminals.remove(terminal);
				break;
			}
		}
	}

	public void setCardChannelFilter(ICardConnectionFilter cardConnectionFilter) {
		this.cardChannelFilter = cardConnectionFilter;
	}

	public void setCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

}
