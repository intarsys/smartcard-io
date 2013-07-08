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

import de.intarsys.tools.attribute.IAttributeSupport;
import de.intarsys.tools.event.INotificationSupport;

/**
 * The abstraction of a single terminal, optionally containing a token,
 * connected to the system.
 * <p>
 * {@link ICardTerminal} monitors the state of the associated terminal / token
 * in a thread of its own as long as connected. In this thread the PCSC
 * GetStatusChange is performed as well as the Connect and BeginTransaction
 * operations.
 * <p>
 * {@link ICardTerminal} is used in a multithreaded environment and its state is
 * accessed by either its monitoring thread or the client code .
 */
public interface ICardTerminal extends INotificationSupport, IAttributeSupport {

	public abstract void addSecondaryResourceFinalizer(Runnable runnable);

	public abstract ICardConnection connectDirect() throws CardException;

	/**
	 * Dispose all of the objects resources. This method should be safe to be
	 * called in any object state, especially when the object is in an
	 * invalid/error state and when the object is already disposed.
	 */
	public void dispose();

	public abstract void freeSecondaryResources();

	/**
	 * The {@link ICard} currently inserted or null.
	 * 
	 * @return The {@link ICard} currently inserted or null.
	 */
	public abstract ICard getCard();

	/**
	 * The {@link ICardSystem} that created this {@link ICardTerminal}.
	 * 
	 * @return The {@link ICardSystem} that created this {@link ICardTerminal}.
	 */
	public ICardSystem getCardSystem();

	/**
	 * The name of the {@link ICardTerminal}.
	 * 
	 * @return The name of the {@link ICardTerminal}.
	 */
	public abstract String getName();

	/**
	 * The state of the {@link ICardTerminal}.
	 * 
	 * @return The state of the {@link ICardTerminal}.
	 */
	public abstract EnumCardTerminalState getState();

	/**
	 * <code>true</code> if this object is already disposed.
	 * 
	 * @return
	 */
	public boolean isDisposed();
}