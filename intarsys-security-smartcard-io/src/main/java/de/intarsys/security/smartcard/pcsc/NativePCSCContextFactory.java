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
package de.intarsys.security.smartcard.pcsc;

import java.util.ArrayList;
import java.util.List;

import de.intarsys.security.smartcard.pcsc.nativec._PCSC;
import de.intarsys.tools.component.SingletonClass;
import de.intarsys.tools.system.SystemTools;
import de.intarsys.tools.yalf.api.ILogger;
import de.intarsys.tools.yalf.api.Level;

/**
 * An {@link IPCSCContextFactory} wrapping native libraries.
 * 
 */
@SingletonClass
public class NativePCSCContextFactory implements IPCSCContextFactory {

	private static ILogger Log = PACKAGE.Log;

	private static final NativePCSCContextFactory ACTIVE = new NativePCSCContextFactory();

	public static final String SYSTEM_DEFAULT_LIBRARY = _PCSC.SYSTEM_DEFAULT_LIBRARY;

	public static NativePCSCContextFactory get() {
		return ACTIVE;
	}

	private final List<INativePCSCLib> libraries;

	private NativePCSCContextFactory() {
		libraries = new ArrayList<INativePCSCLib>(2);
	}

	@Override
	public IPCSCContext establishContext() throws PCSCException {
		if (libraries.isEmpty()) {
			Log.log(Level.INFO, "loading PC/SC system default library"); //$NON-NLS-1$
			NativePCSCLib lib = new NativePCSCLib();
			lib.setPath(SYSTEM_DEFAULT_LIBRARY);
			lib.setUseExecutorThread(false);
			lib.setUseBlockingGetStatusChange(SystemTools.isWindows());
			lib.initialize();
		}
		List<IPCSCContext> contexts = new ArrayList<IPCSCContext>(libraries.size());
		PCSCException cause = null;
		for (INativePCSCLib pcsclib : libraries) {
			IPCSCContext context;

			try {
				context = pcsclib.establishContext();
				contexts.add(context);
			} catch (PCSCException e) {
				cause = e;
				Log.log(Level.SEVERE, "Failed to establish PC/SC context", e); //$NON-NLS-1$
			}
		}
		if (contexts.size() == 0) {
			if (cause == null) {
				throw new PCSCException("Failed to establish PC/SC context"); //$NON-NLS-1$
			} else {
				throw cause;
			}
		}
		if (contexts.size() == 1) {
			return contexts.get(0);
		}
		return new PCSCMultiContext(this, contexts);
	}

	public List<INativePCSCLib> getLibraries() {
		return new ArrayList<>(libraries);
	}

	public void registerLibrary(INativePCSCLib pcsclib) {
		if (Log.isLoggable(Level.TRACE)) {
			Log.trace("Adding library " + pcsclib); //$NON-NLS-1$
		}
		libraries.add(pcsclib);
	}

	@Override
	public String toString() {
		return "PCSC context factory"; //$NON-NLS-1$
	}

}
