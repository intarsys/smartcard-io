/*
 * Copyright (c) 2013, intarsys GmbH
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

import de.intarsys.security.smartcard.card.ICardSystem.DefaultResolver;
import de.intarsys.security.smartcard.card.standard.StandardCardSystem;
import de.intarsys.security.smartcard.pcsc.PCSCContextFactory;
import de.intarsys.tools.event.AttributeChangedEvent;
import de.intarsys.tools.event.INotificationSupport;
import de.intarsys.tools.servicelocator.IServiceResolver;
import de.intarsys.tools.servicelocator.ServiceImplementation;

/**
 * The abstraction of the smartcard environment connected to the system. The
 * {@link ICardSystem} may interface to native PCSC, using the javax.smartcardio
 * or whatever.
 * <p>
 * {@link ICardSystem} monitors the card environment in a monitoring thread.
 * Token events (found and removed) are propagated to registered listeners as
 * {@link AttributeChangedEvent} named "ATTR_CARD_TERMINALS".
 * <p>
 * {@link ICardSystem} is used in a multithreaded environment and its state is
 * accessed by either the monitoring thread or the client code requesting the
 * {@link ICardTerminal} instances.
 */
@ServiceImplementation(defaultResolver = DefaultResolver.class)
public interface ICardSystem extends INotificationSupport {

	public static class DefaultResolver implements IServiceResolver<ICardSystem> {
		@Override
		public ICardSystem apply(Class<ICardSystem> t) {
			return new StandardCardSystem(PCSCContextFactory.get());
		}
	}

	public static final String ATTR_CARD_TERMINALS = "cardTerminals";

	/**
	 * Dispose all of the objects resources. This method should be safe to be
	 * called in any object state, especially when the object is in an
	 * invalid/error state and when the object is already disposed.
	 */
	public void dispose();

	/**
	 * The card terminal with the requested name. The {@link ICardTerminal}
	 * instances preserve identity.
	 * 
	 * @param name
	 * @return The card terminal with the requested name.
	 */
	public ICardTerminal getCardTerminal(String name);

	/**
	 * The {@link ICardTerminal} instances currently available. The
	 * {@link ICardTerminal} instances preserve identity.
	 * 
	 * @return The {@link ICardTerminal} instances currently available.
	 */
	public ICardTerminal[] getCardTerminals();

	/**
	 * <code>true</code> if this object is already disposed.
	 * 
	 * @return
	 */
	public boolean isDisposed();

	/**
	 * <code>true</code> if this object is currently enabled.
	 * 
	 * @return
	 */
	public boolean isEnabled();

	/**
	 * Enable/disable the {@link ICardSystem}.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);

}
