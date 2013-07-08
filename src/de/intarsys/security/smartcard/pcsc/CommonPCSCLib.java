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

import javax.annotation.PostConstruct;

import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.security.smartcard.pcsc.nativec._PCSC;
import de.intarsys.security.smartcard.pcsc.nativec._PCSCThreadedExecutor;
import de.intarsys.tools.system.SystemTools;

abstract public class CommonPCSCLib implements IPCSCLib {

	private _IPCSC pcsc;

	private String path;

	private boolean useExecutorThread = false;

	private boolean useBlockingGetStatusChange = SystemTools.isWindows();

	public CommonPCSCLib() {
	}

	@Override
	public void createNativeWrapper() {
		_PCSC nativeWrapper = new _PCSC(path);
		if (useExecutorThread) {
			pcsc = new _PCSCThreadedExecutor(nativeWrapper);
		} else {
			pcsc = nativeWrapper;
		}
	}

	@Override
	public String getPath() {
		return path;
	}

	public _IPCSC getPcsc() {
		return pcsc;
	}

	@PostConstruct
	public void initializeAfterConstruction() {
		NativePCSCSystem.get().registerLibrary(this);
	}

	@Override
	public boolean isUseBlockingGetStatusChange() {
		return useBlockingGetStatusChange;
	}

	@Override
	public boolean isUseExecutorThread() {
		return useExecutorThread;
	}

	@Override
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public void setUseBlockingGetStatusChange(boolean useBlockingGetStatusChange) {
		this.useBlockingGetStatusChange = useBlockingGetStatusChange;
	}

	@Override
	public void setUseExecutorThread(boolean useExecutorThread) {
		this.useExecutorThread = useExecutorThread;
	}
}
