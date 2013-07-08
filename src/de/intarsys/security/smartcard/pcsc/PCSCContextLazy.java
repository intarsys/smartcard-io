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

import static de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES.SCARD_E_NO_READERS_AVAILABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.NativeBuffer;
import de.intarsys.nativec.type.NativeLongLP64;
import de.intarsys.nativec.type.NativeString;
import de.intarsys.security.smartcard.pcsc.nativec.NativePcscDword;
import de.intarsys.security.smartcard.pcsc.nativec.SCARDCONTEXT;
import de.intarsys.security.smartcard.pcsc.nativec.SCARDHANDLE;
import de.intarsys.security.smartcard.pcsc.nativec.SCARD_READERSTATE;
import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES;
import de.intarsys.tools.string.StringTools;

public class PCSCContextLazy extends CommonPCSCContext {

	private static final Logger Log = PACKAGE.Log;

	private static final int MAX_RETRIES = 3;

	private int connectCounter = 0;

	private int contextLazyReference = 0;

	private SCARDCONTEXT hContextLazy;

	private boolean interrupted = false;

	private List<IPCSCCardReader> lastReaderList;

	private long statusChangeInterval = 60000;

	public PCSCContextLazy(IPCSCLib lib, _IPCSC pcsc) {
		super(lib, pcsc);
	}

	protected SCARDCONTEXT acquireLazy() throws PCSCException {
		synchronized (lockContext) {
			contextLazyReference++;
			if (getHContext() == null) {
				if (hContextLazy == null) {
					hContextLazy = basicAcquire();
				}
				return hContextLazy;
			}
			return getHContext();
		}
	}

	public void cancelGetStatusChange() throws PCSCException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " cancel getStatusChange"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		synchronized (lock) {
			interrupted = true;
			lock.notifyAll();
		}
	}

	@Override
	public PCSCConnection connect(String readerName, int shareMode, int protocol)
			throws PCSCException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " connect"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		SCARDCONTEXT tempContext = acquireLazy();
		NativeString nReaderName = new NativeString(readerName);
		NativeLongLP64 phCard = new NativeLongLP64();
		NativePcscDword activeProtocol = new NativePcscDword();

		try {
			synchronized (lock) {
				connectCounter++;
			}
			int rc = getPcsc().SCardConnect(tempContext, nReaderName,
					shareMode, protocol, phCard, activeProtocol);
			PCSCTools.checkReturnCode(rc);
			INativeHandle protocolHandle = getProtocolHandle(activeProtocol
					.intValue());
			// acquire once more for the connection...
			acquireLazy();
			return new PCSCConnection(this,
					new SCARDHANDLE(phCard.longValue()), shareMode, protocol,
					protocolHandle);
		} finally {
			releaseLazy();
			synchronized (lock) {
				connectCounter--;
			}
		}
	}

	@Override
	public void dispose() throws PCSCException {
		synchronized (lockContext) {
			if (contextLazyReference > 0) {
				hContextLazy = hContext;
				hContext = null;
				return;
			}
			super.dispose();
		}
	}

	@Override
	protected void fromConnectionDisconnect(PCSCConnection connection,
			long disposition) throws PCSCException {
		try {
			super.fromConnectionDisconnect(connection, disposition);
		} finally {
			releaseLazy();
		}
	}

	@Override
	public void getStatusChange(SCARD_READERSTATE readerState,
			int millisecTimeout) throws PCSCException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " get status change"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			long start = System.currentTimeMillis();
			synchronized (lock) {
				interrupted = false;
			}
			int currentState;
			currentState = readerState.getCurrentState();
			while (readerState.getEventState() == currentState) {
				if ((readerState.getEventState() & _IPCSC.SCARD_STATE_PRESENT) == 0) {
					// no card found yet
					readerState.setCurrentState(_IPCSC.SCARD_STATE_UNAWARE);
					try {
						SCARDCONTEXT tempContext = acquireLazy();
						int rc = 0;
						// do your best
						for (int retries = 0; retries <= MAX_RETRIES; retries++) {
							rc = getPcsc().SCardGetStatusChange(tempContext, 0,
									readerState, 1);
							if (readerState.getEventState() != 0) {
								break;
							}
							if (retries < MAX_RETRIES) {
								// du no ask me.... but it helps...
								NativePcscDword readersSize = new NativePcscDword(
										0);
								// force re-read, seems to be a kobil....
								rc = getPcsc().SCardListReaders(tempContext,
										null, null, readersSize);
							}
						}
						PCSCTools.checkReturnCode(rc);
					} finally {
						releaseLazy();
					}
				}
				long stop = System.currentTimeMillis();
				if (millisecTimeout >= 0 && stop - start >= millisecTimeout) {
					return;
				}
				if (readerState.getEventState() == currentState) {
					synchronized (lock) {
						if (interrupted) {
							// reset interrupted flag on daemon
							throw new PCSCException(
									_PCSC_RETURN_CODES.SCARD_E_CANCELLED);
						}
						try {
							lock.wait(1000);
						} catch (InterruptedException e) {
							throw new PCSCException(
									_PCSC_RETURN_CODES.SCARD_E_CANCELLED);
						}
						if (interrupted) {
							// reset interrupted flag on daemon
							throw new PCSCException(
									_PCSC_RETURN_CODES.SCARD_E_CANCELLED);
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

	public long getStatusChangeInterval() {
		return statusChangeInterval;
	}

	@Override
	protected boolean isReleaseVeto() {
		synchronized (lock) {
			return connectCounter > 0;
		}
	}

	@Override
	public List<IPCSCCardReader> listReaders() throws PCSCException {
		if (lastReaderList != null && lastReaderList.size() > 0) {
			return new ArrayList<IPCSCCardReader>(lastReaderList);
		}
		SCARDCONTEXT tempContext = acquireLazy();
		try {
			NativePcscDword readersSize = new NativePcscDword(0);
			int rc = getPcsc().SCardListReaders(tempContext, null, null,
					readersSize);
			if (rc == SCARD_E_NO_READERS_AVAILABLE) {
				return Collections.emptyList();
			}
			PCSCTools.checkReturnCode(rc);
			int size = readersSize.intValue();
			NativeBuffer readerNames = new NativeBuffer(size);
			rc = getPcsc().SCardListReaders(tempContext, null, readerNames,
					readersSize);
			PCSCTools.checkReturnCode(rc);
			List<IPCSCCardReader> readerList = new ArrayList<IPCSCCardReader>(3);
			for (int i = 0; i < size; i++) {
				String name = readerNames.getString(i);
				if (!StringTools.isEmpty(name)) {
					readerList.add(new PCSCCardReader(this, name));
					i += name.length();
				}
			}
			lastReaderList = new ArrayList<IPCSCCardReader>(readerList);
			return readerList;
		} finally {
			releaseLazy();
		}
	}

	protected void releaseLazy() throws PCSCException {
		synchronized (lockContext) {
			contextLazyReference--;
			if (contextLazyReference == 0) {
				if (hContextLazy == null) {
					return;
				}
				basicRelease(hContextLazy);
				hContextLazy = null;
			}
		}
	}

	public void setStatusChangeInterval(long statusChangeInterval) {
		this.statusChangeInterval = statusChangeInterval;
	}

	@Override
	public String toString() {
		return "PCSC Context Kobil"; //$NON-NLS-1$
	}
}
