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
package de.intarsys.security.smartcard.pcsc.nativec;

import java.io.File;

import de.intarsys.nativec.api.INativeFunction;
import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.api.INativeLibrary;
import de.intarsys.nativec.api.NativeInterface;
import de.intarsys.nativec.type.INativeObject;
import de.intarsys.nativec.type.NativeBuffer;
import de.intarsys.nativec.type.NativeLongLP64;
import de.intarsys.nativec.type.NativeLongLP64Type;
import de.intarsys.nativec.type.NativeString;
import de.intarsys.nativec.type.NativeVoid;
import de.intarsys.tools.string.StringTools;
import de.intarsys.tools.system.SystemTools;

/**
 * The concrete intarsys native-c based PC/SC wrapper declaration.
 * 
 * The implementation by default auto-detects the platform and selects the
 * native library. As an alternative you can provide an explicit native library
 * name.
 * 
 */
public class _PCSC implements _IPCSC {

	public static final String SYSTEM_DEFAULT_LIBRARY = ""; //$NON-NLS-1$

	private static final String SCARD_BEGIN_TRANSACTION = "SCardBeginTransaction"; //$NON-NLS-1$
	private static final String SCARD_CANCEL = "SCardCancel"; //$NON-NLS-1$
	private static final String SCARD_CONNECT = "SCardConnect"; //$NON-NLS-1$
	private static final String SCARD_RECONNECT = "SCardReconnect"; //$NON-NLS-1$
	private static final String SCARD_CONTROL = "SCardControl"; //$NON-NLS-1$
	private static final String SCARD_DISCONNECT = "SCardDisconnect"; //$NON-NLS-1$
	private static final String SCARD_END_TRANSACTION = "SCardEndTransaction"; //$NON-NLS-1$
	private static final String SCARD_ESTABLISH_CONTEXT = "SCardEstablishContext"; //$NON-NLS-1$
	private static final String SCARD_GET_ATTRIB = "SCardGetAttrib"; //$NON-NLS-1$
	private static final String SCARD_GET_STATUS_CHANGE = "SCardGetStatusChange"; //$NON-NLS-1$
	private static final String SCARD_LIST_READERS = "SCardListReaders"; //$NON-NLS-1$
	private static final String SCARD_RELEASE_CONTEXT = "SCardReleaseContext"; //$NON-NLS-1$
	private static final String SCARD_STATUS = "SCardStatus"; //$NON-NLS-1$
	private static final String SCARD_TRANSMIT = "SCardTransmit"; //$NON-NLS-1$

	private static final String SCARD_PCI_RAW_NAME = "g_rgSCardRawPci"; //$NON-NLS-1$
	private static final String SCARD_PCI_T0_NAME = "g_rgSCardT0Pci"; //$NON-NLS-1$
	private static final String SCARD_PCI_T1_NAME = "g_rgSCardT1Pci"; //$NON-NLS-1$

	private INativeLibrary lib;

	private String pcscLibraryPath;
	/**
	 * constant pointer to global structures SCARD_PCI_RAW, SCARD_PCI_T0,
	 * SCARD_PCI_T1
	 */
	private INativeHandle scardPciRaw;
	private INativeHandle scardPciT0;
	private INativeHandle scardPciT1;

	private INativeFunction callSCardBeginTransaction;
	private INativeFunction callSCardCancel;
	private INativeFunction callSCardConnect;
	private INativeFunction callSCardReconnect;
	private INativeFunction callSCardControl;
	private INativeFunction callSCardDisconnect;
	private INativeFunction callSCardEndTransaction;
	private INativeFunction callSCardEstablishContext;
	private INativeFunction callSCardGetAttrib;
	private INativeFunction callSCardGetStatusChange;
	private INativeFunction callSCardListReaders;
	private INativeFunction callSCardReleaseContext;
	private INativeFunction callSCardTransmit;
	private INativeFunction callSCardStatus;

	public _PCSC(String pcscLibrary) {
		String defaultLibrary;

		/*
		 * for the native library we must load the one specific to the os we are
		 * running the VM on
		 */
		if (SystemTools.isWindows()) {
			defaultLibrary = "winscard"; //$NON-NLS-1$
		} else {
			// on OS X this assumes a home-made library; see below
			defaultLibrary = "pcsclite"; //$NON-NLS-1$
		}
		if (StringTools.isEmpty(pcscLibrary)) {
			// not home-made after all; see above
			if (SystemTools.isMac()) {
				pcscLibraryPath = "PCSC"; //$NON-NLS-1$
			} else {
				pcscLibraryPath = SystemTools.mapLibraryName(defaultLibrary, 1);
			}
		} else {
			File file;

			file = new File(pcscLibrary);
			if (file.isDirectory()) {
				file = new File(file, SystemTools.mapLibraryName(
						defaultLibrary, 1));
				pcscLibraryPath = file.getAbsolutePath();
			} else {
				pcscLibraryPath = pcscLibrary;
			}
		}
		init();
	}

	@Override
	public INativeHandle getSCARD_PCI_RAW() {
		return scardPciRaw;
	}

	@Override
	public INativeHandle getSCARD_PCI_T0() {
		return scardPciT0;
	}

	@Override
	public INativeHandle getSCARD_PCI_T1() {
		return scardPciT1;
	}

	private void init() {
		lib = NativeInterface.get().createLibrary(pcscLibraryPath);
		// the function names only depend on the library loaded
		// no need to use PCSCTools.isPCSCLite() here!
		if (SystemTools.isWindows()) {
			// use function's ASCII version
			callSCardConnect = lib.getFunction(SCARD_CONNECT + "A"); //$NON-NLS-1$
			callSCardGetStatusChange = lib.getFunction(SCARD_GET_STATUS_CHANGE
					+ "A"); //$NON-NLS-1$
			callSCardListReaders = lib.getFunction(SCARD_LIST_READERS + "A"); //$NON-NLS-1$
			callSCardStatus = lib.getFunction(SCARD_STATUS + "A"); //$NON-NLS-1$
		} else {
			callSCardConnect = lib.getFunction(SCARD_CONNECT);
			callSCardGetStatusChange = lib.getFunction(SCARD_GET_STATUS_CHANGE);
			callSCardListReaders = lib.getFunction(SCARD_LIST_READERS);
			callSCardStatus = lib.getFunction(SCARD_STATUS);
		}
		callSCardReconnect = lib.getFunction(SCARD_RECONNECT);
		callSCardBeginTransaction = lib.getFunction(SCARD_BEGIN_TRANSACTION);
		callSCardCancel = lib.getFunction(SCARD_CANCEL);
		if (SystemTools.isMac()) {
			try {
				callSCardControl = lib.getFunction(SCARD_CONTROL + "132"); //$NON-NLS-1$
			} catch (UnsatisfiedLinkError ignore) {
				// SCardControl will not be available
			}
		} else {
			callSCardControl = lib.getFunction(SCARD_CONTROL);
		}
		callSCardDisconnect = lib.getFunction(SCARD_DISCONNECT);
		callSCardEndTransaction = lib.getFunction(SCARD_END_TRANSACTION);
		callSCardEstablishContext = lib.getFunction(SCARD_ESTABLISH_CONTEXT);
		callSCardGetAttrib = lib.getFunction(SCARD_GET_ATTRIB);
		callSCardReleaseContext = lib.getFunction(SCARD_RELEASE_CONTEXT);
		callSCardTransmit = lib.getFunction(SCARD_TRANSMIT);

		scardPciT0 = lib.getGlobal(SCARD_PCI_T0_NAME);
		scardPciT1 = lib.getGlobal(SCARD_PCI_T1_NAME);
		scardPciRaw = lib.getGlobal(SCARD_PCI_RAW_NAME);
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardBeginTransaction(SCARDHANDLE hCard);
	 * </pre>
	 */
	@Override
	public int SCardBeginTransaction(final SCARDHANDLE hCard) {
		Number rc = callSCardBeginTransaction.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hCard.longValue()));
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardCancel(SCARDCONTEXT hContext);
	 * </pre>
	 */
	@Override
	public int SCardCancel(final SCARDCONTEXT hContext) {
		Number rc = callSCardCancel.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hContext.longValue()));
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardConnect(SCARDCONTEXT hContext,
	 * 		LPCSTR szReader,
	 * 		DWORD dwShareMode,
	 * 		DWORD dwPreferredProtocols,
	 * 		LPSCARDHANDLE phCard, LPDWORD pdwActiveProtocol);
	 * </pre>
	 */
	@Override
	public int SCardConnect(final SCARDCONTEXT hContext,
			final NativeString szReader, final long dwShareMode,
			final long dwPreferredProtocols, final NativeLongLP64 phCard,
			final NativePcscDword pdwActiveProtocol) {
		Number rc = callSCardConnect.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hContext.longValue()), szReader,
				NativePcscDwordType.coerce(dwShareMode),
				NativePcscDwordType.coerce(dwPreferredProtocols), phCard,
				pdwActiveProtocol);
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardControl(SCARDHANDLE hCard, DWORD dwControlCode,
	 * 		LPCVOID pbSendBuffer, DWORD cbSendLength,
	 * 		LPVOID pbRecvBuffer, DWORD cbRecvLength, LPDWORD lpBytesReturned);
	 * </pre>
	 */
	@Override
	public int SCardControl(final SCARDHANDLE hCard, final long dwControlCode,
			final NativeBuffer pbSendBuffer, final long cbSendLength,
			final NativeBuffer pbRecvBuffer, final long cbRecvLength,
			final NativePcscDword lpBytesReturned) {
		if (callSCardControl == null) {
			lpBytesReturned.setValue(0);
			return 0;
		}
		Number rc = callSCardControl.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hCard.longValue()),
				NativePcscDwordType.coerce(dwControlCode), pbSendBuffer,
				NativePcscDwordType.coerce(cbSendLength), pbRecvBuffer,
				NativePcscDwordType.coerce(cbRecvLength), lpBytesReturned);
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardDisconnect(SCARDHANDLE hCard, DWORD dwDisposition);
	 * </pre>
	 */
	@Override
	public int SCardDisconnect(final SCARDHANDLE hCard, final long dwDisposition) {
		Number rc = callSCardDisconnect.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hCard.longValue()),
				NativePcscDwordType.coerce(dwDisposition));
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardEndTransaction(SCARDHANDLE hCard, DWORD dwDisposition);
	 * </pre>
	 */
	@Override
	public int SCardEndTransaction(final SCARDHANDLE hCard,
			final long dwDisposition) {
		Number rc = callSCardEndTransaction.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hCard.longValue()),
				NativePcscDwordType.coerce(dwDisposition));
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardEstablishContext(DWORD dwScope,
	 * 		LPCVOID pvReserved1, LPCVOID pvReserved2, LPSCARDCONTEXT phContext);
	 * </pre>
	 */
	@Override
	public int SCardEstablishContext(final long dwScope,
			final NativeLongLP64 phContext) {
		Number rc = callSCardEstablishContext.invoke(
				NativePcscDwordType.primitiveClass(),
				NativePcscDwordType.coerce(dwScope), NativeVoid.NULL,
				NativeVoid.NULL, phContext);
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardGetAttrib(SCARDHANDLE hCard, DWORD dwAttrId,
	 * 			LPBYTE pbAttr, LPDWORD pcbAttrLen);
	 * </pre>
	 */
	@Override
	public int SCardGetAttrib(final SCARDHANDLE hCard, long dwAttrId,
			final NativeBuffer pbAttr, final NativePcscDword pcbAttrLength) {
		Number rc = callSCardGetAttrib.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hCard.longValue()),
				NativePcscDwordType.coerce(dwAttrId), pbAttr, pcbAttrLength);
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardGetStatusChange(SCARDCONTEXT hContext,
	 * 		DWORD dwTimeout,
	 * 		LPSCARD_READERSTATE_A rgReaderStates, DWORD cReaders);
	 * </pre>
	 */
	@Override
	public int SCardGetStatusChange(final SCARDCONTEXT hContext,
			final long dwTimeout, final INativeObject rgReaderStates,
			final long cReaders) {
		Number rc = callSCardGetStatusChange.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hContext.longValue()),
				NativePcscDwordType.coerce(dwTimeout), rgReaderStates,
				NativePcscDwordType.coerce(cReaders));
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardListReaders(SCARDCONTEXT hContext,
	 * 		LPCSTR mszGroups,
	 * 		LPSTR mszReaders, LPDWORD pcchReaders);
	 * </pre>
	 */
	@Override
	public int SCardListReaders(final SCARDCONTEXT hContext,
			final NativeString mszGroups, final INativeObject mszReaders,
			final NativePcscDword pcchReaders) {
		Number rc = callSCardListReaders.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hContext.longValue()), mszGroups,
				mszReaders, pcchReaders);
		return rc.intValue();
	}

	/**
	 * <pre>
	 * LONG SCardReconnect(SCARDHANDLE hCard, DWORD dwShareMode,
	 * 		DWORD dwPreferredProtocols, DWORD dwInitialization,
	 * 		LPDWORD pdwActiveProtocol);
	 * </pre>
	 */
	@Override
	public int SCardReconnect(SCARDHANDLE hCard, long dwShareMode,
			long dwPreferredProtocols, long dwInitialization,
			NativePcscDword pdwActiveProtocol) {
		Number rc = callSCardReconnect
				.invoke(NativePcscDwordType.primitiveClass(),
						NativeLongLP64Type.coerce(hCard.longValue()),
						NativePcscDwordType.coerce(dwShareMode),
						NativePcscDwordType.coerce(dwPreferredProtocols),
						NativePcscDwordType.coerce(dwInitialization),
						pdwActiveProtocol);
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardReleaseContext(SCARDCONTEXT hContext);
	 * </pre>
	 */
	@Override
	public int SCardReleaseContext(final SCARDCONTEXT hContext) {
		Number rc = callSCardReleaseContext.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hContext.longValue()));
		return rc.intValue();
	}

	/**
	 * <pre>
	 * LONG SCardStatus	(	SCARDHANDLE 	hCard,
	 * LPSTR 	mszReaderName,
	 * LPDWORD 	pcchReaderLen,
	 * LPDWORD 	pdwState,
	 * LPDWORD 	pdwProtocol,
	 * LPBYTE 	pbAtr,
	 * LPDWORD 	pcbAtrLen 
	 * )
	 * </pre>
	 */
	@Override
	public int SCardStatus(final SCARDHANDLE hCard,
			final INativeObject szReaderName,
			final NativePcscDword pcchReaderLen,
			final NativePcscDword pdwState, final NativePcscDword pdwProtocol,
			final NativeBuffer pbAtr, final NativePcscDword pcbAtrLen) {
		Number rc = callSCardStatus.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hCard.longValue()), szReaderName,
				pcchReaderLen, pdwState, pdwProtocol, pbAtr, pcbAtrLen);
		return rc.intValue();
	}

	/**
	 * <pre>
	 * PCSC_API LONG SCardTransmit(SCARDHANDLE hCard,
	 * 		LPCSCARD_IO_REQUEST pioSendPci,
	 * 		LPCBYTE pbSendBuffer, DWORD cbSendLength,
	 * 		LPSCARD_IO_REQUEST pioRecvPci,
	 * 		LPBYTE pbRecvBuffer, LPDWORD pcbRecvLength);
	 * </pre>
	 */
	@Override
	public int SCardTransmit(final SCARDHANDLE hCard,
			final INativeHandle pioSendPci, final INativeObject pbSendBuffer,
			final long cbSendLength, final INativeObject pioRecvPci,
			final NativeBuffer pbRecvBuffer, final NativePcscDword pcbRecvLength) {
		Number rc = callSCardTransmit.invoke(
				NativePcscDwordType.primitiveClass(),
				NativeLongLP64Type.coerce(hCard.longValue()), pioSendPci,
				pbSendBuffer, NativePcscDwordType.coerce(cbSendLength),
				pioRecvPci, pbRecvBuffer, pcbRecvLength);
		return rc.intValue();
	}
}
