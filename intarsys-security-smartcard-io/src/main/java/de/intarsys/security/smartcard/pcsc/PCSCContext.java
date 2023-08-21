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

import static de.intarsys.security.smartcard.pcsc.PCSCTools.BufferHelper;
import static de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES.SCARD_E_NO_READERS_AVAILABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.NativeLongLP64;
import de.intarsys.nativec.type.NativeString;
import de.intarsys.security.smartcard.pcsc.nativec.NativePcscDword;
import de.intarsys.security.smartcard.pcsc.nativec.SCARDCONTEXT;
import de.intarsys.security.smartcard.pcsc.nativec.SCARDHANDLE;
import de.intarsys.security.smartcard.pcsc.nativec.SCARD_READERSTATE;
import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * The default {@link IPCSCContext} implementation.
 * 
 */
public class PCSCContext extends CommonPCSCContext {

	private static final ILogger Log = PACKAGE.Log;

	private int connectCounter = 0;

	private boolean interrupted;

	public PCSCContext(INativePCSCLib lib, _IPCSC pcsc) throws PCSCException {
		this(lib, pcsc, establish(pcsc));
	}

	public PCSCContext(INativePCSCLib lib, _IPCSC pcsc, SCARDCONTEXT pHContext) {
		super(lib, pcsc, pHContext);
	}

	public void cancelGetStatusChange() throws PCSCException {
		Log.trace("{} cancel getStatusChange", this); //$NON-NLS-1$
		if (isUseBlockingGetStatusChange()) {
			SCARDCONTEXT tempContext = getHContext();
			// disposed?
			if (tempContext != null) {
				int rc = getPcsc().SCardCancel(tempContext);
				if (!isDisposed()) {
					PCSCException.checkReturnCode(rc);
				}
			}
		} else {
			synchronized (lock) {
				interrupted = true;
				lock.notifyAll();
			}
		}
	}

	@Override
	public IPCSCConnection connect(String id, String readerName, int shareMode, int protocol) throws PCSCException {
		Log.trace("{} connect id {}, {}, {}, {}", this, id, readerName, shareMode, protocol); //$NON-NLS-1$
		NativeString nReaderName = new NativeString(readerName);
		NativeLongLP64 phCard = new NativeLongLP64();
		NativePcscDword activeProtocol = new NativePcscDword();

		try {
			synchronized (lock) {
				connectCounter++;
			}
			SCARDCONTEXT tempContext = getHContext();
			if (tempContext == null) {
				throw new PCSCException(_PCSC_RETURN_CODES.SCARD_E_INVALID_HANDLE);
			}
			int rc = getPcsc().SCardConnect(tempContext, nReaderName, shareMode, protocol, phCard, activeProtocol);
			PCSCException.checkReturnCode(rc);
		} finally {
			synchronized (lock) {
				connectCounter--;
			}
		}

		INativeHandle protocolHandle = getProtocolHandle(activeProtocol.intValue());
		return new PCSCConnection(this, id, new SCARDHANDLE(phCard.longValue()), shareMode, activeProtocol.intValue(),
				protocolHandle);
	}

	@Override
	protected void getStatusChange(SCARD_READERSTATE readerState, int millisecTimeout) throws PCSCException {
		try {
			long start = System.currentTimeMillis();
			synchronized (lock) {
				interrupted = false;
			}
			int currentState = readerState.getCurrentState();
			while (readerState.getEventState() == currentState) {
				if (!isUseBlockingGetStatusChange()) {
					readerState.setCurrentState(_IPCSC.SCARD_STATE_UNAWARE);
				}
				SCARDCONTEXT tempContext = getHContext();
				if (tempContext == null) {
					throw new PCSCException(_PCSC_RETURN_CODES.SCARD_E_CANCELLED);
				}
				int rc = getPcsc().SCardGetStatusChange(tempContext, millisecTimeout, readerState, 1);
				PCSCException.checkReturnCode(rc);
				if (!isUseBlockingGetStatusChange() && readerState.getEventState() == currentState) {
					synchronized (lock) {
						if (interrupted) {
							throw new PCSCException(_PCSC_RETURN_CODES.SCARD_E_CANCELLED);
						}
						if (millisecTimeout == 0) {
							return;
						}
						long timePassed = System.currentTimeMillis() - start;
						if (millisecTimeout > 0 && timePassed >= millisecTimeout) {
							throw new PCSCException(_PCSC_RETURN_CODES.SCARD_E_TIMEOUT);
						}
						try {
							lock.wait(1000);
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							throw new PCSCException(_PCSC_RETURN_CODES.SCARD_E_CANCELLED);
						}
						if (interrupted) {
							throw new PCSCException(_PCSC_RETURN_CODES.SCARD_E_CANCELLED);
						}
					}
				}
			}
		} finally {
			synchronized (lock) {
				interrupted = false;
			}
		}
	}

	@Override
	protected boolean isReleaseVeto() {
		synchronized (lock) {
			return connectCounter > 0;
		}
	}

	@Override
	public List<IPCSCCardReader> listReaders() throws PCSCException {
		SCARDCONTEXT nativeContext = getHContext();
		if (nativeContext == null) {
			return Collections.emptyList();
		}
		byte[] result;
		try {
			result = BufferHelper.call(this,
					(buffer, bufferSize) -> getPcsc().SCardListReaders(nativeContext, null, buffer, bufferSize));
		} catch (PCSCException ex) {
			if (ex.getErrorCode() == SCARD_E_NO_READERS_AVAILABLE) {
				return Collections.emptyList();
			}
			throw ex;
		}

		List<IPCSCCardReader> readerList = new ArrayList<IPCSCCardReader>(3);
		int start = 0;
		for (int i = 0; i < result.length; i++) {
			if (result[i] == 0) {
				if (i > start) {
					readerList.add(new PCSCCardReader(this, new String(result, start, i - start)));
				}
				start = i + 1;
			}
		}
		return readerList;
	}
}
