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

import javax.annotation.PostConstruct;

import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.security.smartcard.pcsc.nativec._PCSC;
import de.intarsys.security.smartcard.pcsc.nativec._PCSCThreadedExecutor;
import de.intarsys.tools.system.SystemTools;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * A common implementation for a native library, installed in the
 * {@link NativePCSCContextFactory}.
 * 
 * The separation of common code to this superclass was due to the fact that the
 * PC/SC API implementations of some manufacturer were quite poor and needed
 * some special tweaks on each call on the java side...
 * 
 * In these days we only use the standard concrete implementation
 * {@link NativePCSCLib}.
 * 
 */
public abstract class CommonPCSCLib implements INativePCSCLib {

	private static final ILogger Log = PACKAGE.Log;

	private _IPCSC pcsc;

	private String path;

	private boolean useExecutorThread = false;

	private boolean useBlockingGetStatusChange = SystemTools.isWindows();

	public CommonPCSCLib() {
	}

	protected void createNativeWrapper() {
		try {
			_PCSC nativeWrapper = new _PCSC(path);
			if (useExecutorThread) {
				pcsc = new _PCSCThreadedExecutor(nativeWrapper);
			} else {
				pcsc = nativeWrapper;
			}
		} catch (Throwable t) {
			Log.warn("No PC/SC interface available: {}\n{}", t.getMessage(), t);
		}
	}

	public String getPath() {
		return path;
	}

	public _IPCSC getPcsc() {
		return pcsc;
	}

	@PostConstruct
	public void initialize() {
		createNativeWrapper();
		NativePCSCContextFactory.get().registerLibrary(this);
	}

	public boolean isUseBlockingGetStatusChange() {
		return useBlockingGetStatusChange;
	}

	public boolean isUseExecutorThread() {
		return useExecutorThread;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setUseBlockingGetStatusChange(boolean useBlockingGetStatusChange) {
		this.useBlockingGetStatusChange = useBlockingGetStatusChange;
	}

	public void setUseExecutorThread(boolean useExecutorThread) {
		this.useExecutorThread = useExecutorThread;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PC/SC library; path ");
		sb.append(getPath());
		sb.append("; executor ");
		sb.append(isUseExecutorThread());
		sb.append("; blocking ");
		sb.append(isUseBlockingGetStatusChange());
		return sb.toString();
	}
}
