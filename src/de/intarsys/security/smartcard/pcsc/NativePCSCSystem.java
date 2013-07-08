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
package de.intarsys.security.smartcard.pcsc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.security.smartcard.pcsc.nativec._PCSC;
import de.intarsys.tools.system.SystemTools;

public class NativePCSCSystem implements IPCSCContextFactory {

	private static Logger Log = PACKAGE.Log;

	final private static NativePCSCSystem ACTIVE = new NativePCSCSystem();

	public static final String SYSTEM_DEFAULT_LIBRARY = _PCSC.SYSTEM_DEFAULT_LIBRARY;

	static public NativePCSCSystem get() {
		return ACTIVE;
	}

	private List<IPCSCLib> pcsclibs;

	private NativePCSCSystem() {
		pcsclibs = new ArrayList<IPCSCLib>(2);
	}

	@Override
	public IPCSCContext establishContext() throws PCSCException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " establish context"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (pcsclibs.isEmpty()) {
			Log.log(Level.INFO, "loading system default library"); //$NON-NLS-1$
			IPCSCLib lib = new PCSCLibStandard();
			lib.setPath(SYSTEM_DEFAULT_LIBRARY);
			lib.setUseExecutorThread(false);
			lib.setUseBlockingGetStatusChange(SystemTools.isWindows());
			registerLibrary(lib);
		}
		List<IPCSCContext> contexts = new ArrayList<IPCSCContext>(
				pcsclibs.size());
		Exception cause = null;
		for (IPCSCLib pcsclib : pcsclibs) {
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
			throw new PCSCException("Failed to establish PC/SC context", cause); //$NON-NLS-1$
		}
		if (contexts.size() == 1) {
			return contexts.get(0);
		}
		return new PCSCMultiContext(this, contexts);
	}

	public void registerLibrary(IPCSCLib pcsclib) {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " adding library " + pcsclib.getPath() + "; executor threads " + pcsclib.isUseExecutorThread() + "; blocking " + pcsclib.isUseBlockingGetStatusChange()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		pcsclib.createNativeWrapper();
		pcsclibs.add(pcsclib);
	}

	@Override
	public String toString() {
		return "PCSC"; //$NON-NLS-1$
	}

}
