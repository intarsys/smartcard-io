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

import de.intarsys.tools.attribute.IAttributeSupport;

/**
 * The abstraction of a smartcard inserted in an {@link ICardTerminal}. An
 * {@link ICard} can be achieved by calling {@link ICardTerminal.getCard()} if
 * present.
 * 
 * Ensure thread safe implementation for {@link IAttributeSupport}!
 */
public interface ICard extends IAttributeSupport {

	/**
	 * Connect to the card exclusive.
	 * @param protocol TODO
	 * 
	 * @return A {@link ICardConnection}
	 */
	public ICardConnection connectExclusive(int protocol) throws CardException;

	/**
	 * Connect to the card asynchronously.
	 * @param protocol TODO
	 * @param connectionCallback
	 *            The callback to be executed upon termination of the operation.
	 * 
	 * @return A Future<ICardChannel> giving access to the ongoing operation.
	 */
	public Future<ICardConnection> connectShared(
			int protocol, IConnectionCallback connectionCallback);

	/**
	 * The {@link ATR} identifying the {@link ICard}.
	 * 
	 * @return The {@link ATR} identifying the {@link ICard}.
	 */
	public ATR getAtr();

	/**
	 * The {@link ICardTerminal} where the {@link ICard} is inserted.
	 * 
	 * @return The {@link ICardTerminal} where the {@link ICard} is inserted.
	 */
	public ICardTerminal getCardTerminal();

	/**
	 * The current state of the {@link ICard}.
	 * 
	 * @return The current state of the {@link ICard}.
	 */
	public EnumCardState getState();

	/**
	 * true if this card is "attached" via contactless reader.
	 * 
	 * @return
	 */
	public boolean isContactless();

}
