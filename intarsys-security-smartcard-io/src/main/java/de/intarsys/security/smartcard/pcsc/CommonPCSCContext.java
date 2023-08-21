/*
 * Copyright (c) 2013, intarsys GmbH
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - Neither the name of intarsys nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific
 * prior written permission.
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

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.NativeLongLP64;
import de.intarsys.nativec.type.NativeString;
import de.intarsys.security.smartcard.pcsc.nativec.SCARDCONTEXT;
import de.intarsys.security.smartcard.pcsc.nativec.SCARD_READERSTATE;
import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES;
import de.intarsys.tools.concurrent.ThreadTools;
import de.intarsys.tools.oid.IOIDGenerator;
import de.intarsys.tools.oid.PronouncableOIDGenerator;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * A common implementation for {@link IPCSCContext}.
 * 
 * The separation of common code to this superclass was due to the fact that the
 * PC/SC API implementations of some manufacturer were quite poor and needed
 * some special tweaks on each call on the java side...
 * 
 * In these days we only use the standard concrete implementation
 * {@link PCSCContext}.
 * 
 */
public abstract class CommonPCSCContext implements IPCSCContext {

	private static final IOIDGenerator<String> OID_GENERATOR = new PronouncableOIDGenerator();

	private static final ILogger Log = PACKAGE.Log;

	private static final AtomicInteger Counter = new AtomicInteger(0);

	protected static SCARDCONTEXT establish(_IPCSC pcsc) throws PCSCException {
		NativeLongLP64 phContext = new NativeLongLP64();
		for (int retry = 0; retry < 3; retry++) {
			int rc = pcsc.SCardEstablishContext(_IPCSC.SCARD_SCOPE_SYSTEM,
					phContext);
			if (rc == _PCSC_RETURN_CODES.ERROR_SEM_TIMEOUT) {
				ThreadTools.sleep(50);
				continue;
			}
			PCSCException.checkReturnCode(rc);
			break;
		}
		SCARDCONTEXT result = new SCARDCONTEXT(phContext.longValue());
		return result;
	}

	protected SCARDCONTEXT hContext;

	private final _IPCSC pcsc;

	private final INativePCSCLib lib;

	protected final Object lock = new Object();

	protected final Object lockContext = new Object();

	private final String id = OID_GENERATOR.createOID();

	private boolean useBlockingGetStatusChange;

	protected CommonPCSCContext(INativePCSCLib pLib, _IPCSC pPcsc, SCARDCONTEXT pHContext) {
		lib = pLib;
		pcsc = pPcsc;
		hContext = pHContext;
		int count = Counter.incrementAndGet();
		Log.debug("{} created, handle {}, {} active", getLogLabel(), Long.toHexString(hContext.longValue()), count);
	}

	protected void basicRelease(SCARDCONTEXT pHContext) throws PCSCException {
		int rc = pcsc.SCardReleaseContext(pHContext);
		PCSCException.checkReturnCode(rc);
	}

	@Override
	public void dispose() throws PCSCException {
		if (isReleaseVeto()) {
			// this check is to defend against deadlock when we try to
			// call release but PCSC is stuck because of a pending connect
			Log.warn("{} not released, connection pending", getLogLabel()); //$NON-NLS-1$
			return;
		}
		int count = Counter.decrementAndGet();
		final SCARDCONTEXT tempContext;
		synchronized (lockContext) {
			if (hContext == null) {
				return;
			}
			tempContext = hContext;
			hContext = null;
		}
		Log.debug("{} release, {} active", getLogLabel(), count); //$NON-NLS-1$
		try {
			basicRelease(tempContext);
		} catch (PCSCException e) {
			Log.debug("{} release exception", getLogLabel(), e); //$NON-NLS-1$
		}
	}

	@Override
	public IPCSCContext establishContext() throws PCSCException {
		return getLib().establishContext();
	}

	protected void fromConnectionBeginTransaction(PCSCConnection connection)
			throws PCSCException {
		int rc = getPcsc().SCardBeginTransaction(connection.getHCard());
		PCSCException.checkReturnCode(rc);
	}

	protected void fromConnectionDisconnect(PCSCConnection connection,
			long disposition) throws PCSCException {
		int rc = getPcsc().SCardDisconnect(connection.getHCard(), disposition);
		try {
			PCSCException.checkReturnCode(rc);
		} catch (PCSCException e) {
			if (isDisposed()) {
				// we may have destroyed the context to shutdown...
				// disconnect may wait for lock forever...
				return;
			}
			throw e;
		}
	}

	protected void fromConnectionEndTransaction(PCSCConnection connection,
			int disposition) throws PCSCException {
		int rc = getPcsc().SCardEndTransaction(connection.getHCard(),
				disposition);
		try {
			PCSCException.checkReturnCode(rc);
		} catch (PCSCException e) {
			if (isDisposed()) {
				// we may have destroyed the context to shutdown...
				// endTransaction may wait for lock forever...
				return;
			}
			throw e;
		}
	}

	public SCARDCONTEXT getHContext() {
		synchronized (lockContext) {
			return hContext;
		}
	}

	public String getId() {
		return id;
	}

	public INativePCSCLib getLib() {
		return lib;
	}

	protected String getLogLabel() {
		return "PCSCContext " + getId(); //$NON-NLS-1$
	}

	public _IPCSC getPcsc() {
		return pcsc;
	}

	protected INativeHandle getProtocolHandle(int protocolId) {
		switch (protocolId) {
		case _IPCSC.SCARD_PROTOCOL_T0:
			return getPcsc().getSCARD_PCI_T0();
		case _IPCSC.SCARD_PROTOCOL_T1:
			return getPcsc().getSCARD_PCI_T1();
		}
		return null;
	}

	protected abstract void getStatusChange(SCARD_READERSTATE newState,
			int millisecTimeout) throws PCSCException;

	@Override
	public final PCSCCardReaderState getStatusChange(String readerName,
			PCSCCardReaderState currentState, int millisecTimeout)
			throws PCSCException, TimeoutException {
		SCARD_READERSTATE newState = new SCARD_READERSTATE();
		newState.setReader(new NativeString(readerName));
		if (currentState != null) {
			newState.setCurrentState(currentState.getEventState());
			newState.setEventState(currentState.getEventState());
			newState.setATR(currentState.getATR());
		} else {
			newState.setCurrentState(_IPCSC.SCARD_STATE_UNAWARE);
			newState.setEventState(_IPCSC.SCARD_STATE_UNAWARE);
		}
		try {
			Log.trace(
					"{} getStatusChange({}), blocking={}", getLogLabel(), readerName, isUseBlockingGetStatusChange()); //$NON-NLS-1$ //$NON-NLS-2$
			getStatusChange(newState, millisecTimeout);
			PCSCCardReaderState state = new PCSCCardReaderState(newState);
			Log.trace("{} getStatusChange(), -> \n{}", getLogLabel(), state.toString()); //$NON-NLS-1$
			return state;
		} catch (PCSCException e) {
			if (e.getErrorCode() == _IPCSC.SCARD_E_TIMEOUT) {
				Log.trace("{} getStatusChange({}) timeout", getLogLabel(), readerName); //$NON-NLS-1$
				throw new TimeoutException(e.getLocalizedMessage());
			}
			throw e;
		}
	}

	@Override
	public boolean isDisposed() {
		synchronized (lockContext) {
			return hContext == null;
		}
	}

	protected boolean isReleaseVeto() {
		return false;
	}

	public boolean isUseBlockingGetStatusChange() {
		return useBlockingGetStatusChange;
	}

	public void setUseBlockingGetStatusChange(boolean useBlockingGetStatusChange) {
		this.useBlockingGetStatusChange = useBlockingGetStatusChange;
	}

	@Override
	public String toString() {
		return getLogLabel();
	}
}
