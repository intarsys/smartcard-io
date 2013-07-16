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
package de.intarsys.security.smartcard.smartcardio;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;

import javax.smartcardio.Card;

/**
 * The intarsys javax.smartcardio alternative.
 * 
 * main javax.smartcardio differences (intended):
 * <ul>
 * <li>dedicated PCSC context for terminals, terminal and card</li>
 * <li>waitForChange(timeout) semantics improved(?), state change is reset even
 * in case of timeout</li>
 * <li>reader insertion is handled, too</li>
 * <li>no finalizer for card!</li>
 * </ul>
 * 
 * open design questions
 * <ul>
 * <li>"exclusive" card access is NOT restricted to a Java thread</li> I found
 * no technical reason for this constraint. In imho this makes handling of
 * shared and pooled resources ugly. Next to "1 PSCS context" this would be the
 * op level reason *not* to use smartcardio. Any opinions?
 * <li>"connect" always returns the identical {@link Card} object</li>This is
 * why? This implementation currently opens a new shared connection. If we
 * revert to the spec, i'd at least expect some kind of reference counting.
 * </ul>
 * 
 * todos
 * <ul>
 * <li>no channel support yet</li>
 * <li>implement "GET RESPONSE"</li>
 * <li>no permission checks so far</li>
 * <li>review exceptions to be standard compliant (IllegalStateException)</li>
 * </ul>
 * 
 * The following intro is borrowed from the fantastic BouncyCastleProvider
 * source.
 * 
 * To add the provider at runtime use:
 * 
 * <pre>
 * import java.security.Security;
 * import de.intarsys.security.smartcard.smartcardio.SmartcardioProvider;
 * 
 * Security.addProvider(new SmartcardioProvider());
 * </pre>
 * 
 * The provider can also be configured as part of your environment via static
 * registration by adding an entry to the java.security properties file (found
 * in $JAVA_HOME/jre/lib/security/java.security, where $JAVA_HOME is the
 * location of your JDK/JRE distribution). You'll find detailed instructions in
 * the file but basically it comes down to adding a line:
 * 
 * <pre>
 * <code>
 *    security.provider.&lt;n&gt;=de.intarsys.security.smartcard.smartcardio.SmartcardioProvider
 * </code>
 * </pre>
 * 
 * Where &lt;n&gt; is the preference you want the provider at (1 being the most
 * preferred).
 * <p>
 * Note: JCE algorithm names should be upper-case only so the case insensitive
 * test for getInstance works.
 * 
 */
final public class SmartcardioProvider extends Provider {

	public SmartcardioProvider() {
		super("intarsys-smartcardio", 1.0d, "intarsys smartcard io");
		AccessController.doPrivileged(new PrivilegedAction<Void>() {
			@Override
			public Void run() {
				put("TerminalFactory.PC/SC",
						"de.intarsys.security.smartcard.smartcardio.SmartcardioTerminalFactory");
				return null;
			}
		});
	}
}
