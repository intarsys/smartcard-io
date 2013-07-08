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

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.INativeObject;
import de.intarsys.nativec.type.NativeBuffer;
import de.intarsys.nativec.type.NativeLongLP64;
import de.intarsys.nativec.type.NativeString;
import de.intarsys.security.smartcard.pcsc.PCSCException;

/**
 * This interface mimics the PCSC-Lite/Winscard API.
 * 
 */
public interface _IPCSC extends _PCSC_RETURN_CODES {

	public static final int CM_IOCTL_GET_FEATURE_REQUEST = 3400;

	public static final int FEATURE_ABORT = 0x0B;

	public static final int FEATURE_GET_KEY_PRESSED = 0x05;

	public static final int FEATURE_IFD_PIN_PROP = 0x0A;

	public static final int FEATURE_MCT_READERDIRECT = 0x08;

	public static final int FEATURE_MCT_UNIVERSAL = 0x09;

	public static final int FEATURE_MODIFY_PIN_DIRECT = 0x07;

	public static final int FEATURE_MODIFY_PIN_FINISH = 0x04;

	public static final int FEATURE_MODIFY_PIN_START = 0x03;

	public static final int FEATURE_VERIFY_PIN_DIRECT = 0x06;

	public static final int FEATURE_VERIFY_PIN_FINISH = 0x02;

	public static final int FEATURE_VERIFY_PIN_START = 0x01;

	public static final int FEATURE_EXECUTE_PACE = 0x20;

	/** Eject the card on close */
	public static final int SCARD_EJECT_CARD = 3;

	/** Don't do anything special on close */
	public static final int SCARD_LEAVE_CARD = 0;

	/** Raw is the active protocol. */
	public static final int SCARD_PROTOCOL_RAW = 0x00010000;

	/** T=0 is the active protocol. */
	public static final int SCARD_PROTOCOL_T0 = 0x00000001;

	/** T=1 is the active protocol. */
	public static final int SCARD_PROTOCOL_T1 = 0x00000002;

	/** This is the mask of ISO defined transmission protocols */
	public static final int SCARD_PROTOCOL_Tx = SCARD_PROTOCOL_T0
			| SCARD_PROTOCOL_T1;

	/** There is no active protocol. */
	public static final int SCARD_PROTOCOL_UNDEFINED = 0x00000000;

	/** Reset the card on close */
	public static final int SCARD_RESET_CARD = 1;

	/**
	 * The context is the system context, and any database operations are
	 * performed within the domain of the system. (The calling application must
	 * have appropriate access permissions for any database actions.)
	 */
	public static final int SCARD_SCOPE_SYSTEM = 2;

	/**
	 * The context is that of the current terminal, and any database operations
	 * are performed within the domain of that terminal. (The calling
	 * application must have appropriate access permissions for any database
	 * actions.)
	 */
	public static final int SCARD_SCOPE_TERMINAL = 1;

	/**
	 * The context is a user context, and any database operations are performed
	 * within the domain of the user.
	 */
	public static final int SCARD_SCOPE_USER = 0;

	/**
	 * This application demands direct control of the reader, so it is not
	 * available to other applications.
	 */
	public static final int SCARD_SHARE_DIRECT = 3;

	/**
	 * This application is not willing to share this card with other
	 * applications.
	 */
	public static final int SCARD_SHARE_EXCLUSIVE = 1;

	/**
	 * This application is willing to share this card with other applications.
	 */
	public static final int SCARD_SHARE_SHARED = 2;

	/**
	 * This implies that there is a card in the reader with an ATR matching one
	 * of the target cards. If this bit is set, SCARD_STATE_PRESENT will also be
	 * set. This bit is only returned on the SCardLocateCard() service.
	 */

	public static final int SCARD_STATE_ATRMATCH = 0x00000040;

	/**
	 * This implies that there is a difference between the state believed by the
	 * application, and the state known by the Service Manager. When this bit is
	 * set, the application may assume a significant state change has occurred
	 * on this reader.
	 */
	public static final int SCARD_STATE_CHANGED = 0x00000002;

	/**
	 * This implies that there is not card in the reader. If this bit is set,
	 * all the following bits will be clear.
	 */
	public static final int SCARD_STATE_EMPTY = 0x00000010;
	/**
	 * This implies that the card in the reader is allocated for exclusive use
	 * by another application. If this bit is set, SCARD_STATE_PRESENT will also
	 * be set.
	 */
	public static final int SCARD_STATE_EXCLUSIVE = 0x00000080;
	/**
	 * The application requested that this reader be ignored. No other bits will
	 * be set.
	 */
	public static final int SCARD_STATE_IGNORE = 0x00000001;
	/**
	 * This implies that the card in the reader is in use by one or more other
	 * applications, but may be connected to in shared mode. If this bit is set,
	 * SCARD_STATE_PRESENT will also be set.
	 */
	public static final int SCARD_STATE_INUSE = 0x00000100;
	/**
	 * This implies that the card in the reader is unresponsive or not supported
	 * by the reader or software.
	 */
	public static final int SCARD_STATE_MUTE = 0x00000200;
	/**
	 * This implies that there is a card in the reader.
	 */
	public static final int SCARD_STATE_PRESENT = 0x00000020;
	/**
	 * This implies that the actual state of this reader is not available. If
	 * this bit is set, then all the following bits are clear.
	 */
	public static final int SCARD_STATE_UNAVAILABLE = 0x00000008;
	/**
	 * The application is unaware of the current state, and would like to know.
	 * The use of this value results in an immediate return from state
	 * transition monitoring services. This is represented by all bits set to
	 * zero.
	 */
	public static final int SCARD_STATE_UNAWARE = 0x00000000;
	/**
	 * This implies that the given reader name is not recognized by the Service
	 * Manager. If this bit is set, then SCARD_STATE_CHANGED and
	 * SCARD_STATE_IGNORE will also be set.
	 */
	public static final int SCARD_STATE_UNKNOWN = 0x00000004;
	/**
	 * This implies that the card in the reader has not been powered up.
	 */
	public static final int SCARD_STATE_UNPOWERED = 0x00000400;

	/** Power down the card on close */
	public static final int SCARD_UNPOWER_CARD = 2;

	/**
	 * Get the reference to the global structure SCARD_PCI_RAW
	 * 
	 * @return constant pointer to global structure SCARD_PCI_T0
	 */
	public abstract INativeHandle getSCARD_PCI_RAW();

	/**
	 * Get the reference to the global structure SCARD_PCI_T0
	 * 
	 * @return constant pointer to global structure SCARD_PCI_T0
	 */
	public abstract INativeHandle getSCARD_PCI_T0();

	/**
	 * Get the reference to the global structure SCARD_PCI_T1
	 * 
	 * @return constant pointer to global structure SCARD_PCI_T1
	 */
	public abstract INativeHandle getSCARD_PCI_T1();

	/**
	 * The SCardBeginTransaction function starts a transaction.
	 * 
	 * The function waits for the completion of all other transactions before it
	 * begins. After the transaction starts, all other applications are blocked
	 * from accessing the smart card while the transaction is in progress.
	 * 
	 * <pre>
	 * LONG SCardBeginTransaction(
	 * 	__in SCARDHANDLE hCard
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * hCard [in]
	 * 
	 * A reference value obtained from a previous call to SCardConnect.
	 */
	public abstract int SCardBeginTransaction(final SCARDHANDLE hCard);

	/**
	 * <pre>
	 * LONG SCardCancel( __in SCARDCONTEXT hContext );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * hContext [in]
	 * 
	 * Handle that identifies the resource manager context. The resource manager
	 * context is set by a previous call to SCardEstablishContext.
	 */

	public int SCardCancel(final SCARDCONTEXT hContext);

	/**
	 * The SCardConnect function establishes a connection (using a specific
	 * resource manager context) between the calling application and a smart
	 * card contained by a specific reader. If no card exists in the specified
	 * reader, an error is returned.
	 * 
	 * LONG SCardConnect( SCARDCONTEXT hContext, LPCTSTR szReader, DWORD
	 * dwShareMode, DWORD dwPreferredProtocols, LPSCARDHANDLE phCard, LPDWORD
	 * pdwActiveProtocol );
	 * 
	 * Parameters
	 * 
	 * hContext [in] A handle that identifies the resource manager context. The
	 * resource manager context is set by a previous call to
	 * SCardEstablishContext.
	 * 
	 * szReader [in] The name of the reader that contains the target card.
	 * 
	 * dwShareMode [in] A flag that indicates whether other applications may
	 * form connections to the card. Value Meaning SCARD_SHARE_SHARED This
	 * application is willing to share the card with other applications.
	 * SCARD_SHARE_EXCLUSIVE This application is not willing to share the card
	 * with other applications. SCARD_SHARE_DIRECT This application is
	 * allocating the reader for its private use, and will be controlling it
	 * directly. No other applications are allowed access to it.
	 * 
	 * dwPreferredProtocols [in] A bitmask of acceptable protocols for the
	 * connection. Possible values may be combined with the OR operation. Value
	 * Meaning SCARD_PROTOCOL_T0 T=0 is an acceptable protocol.
	 * SCARD_PROTOCOL_T1 T=1 is an acceptable protocol. 0 This parameter may be
	 * zero only if dwShareMode is set to SCARD_SHARE_DIRECT. In this case, no
	 * protocol negotiation will be performed by the drivers until an
	 * IOCTL_SMARTCARD_SET_PROTOCOL control directive is sent with SCardControl.
	 * 
	 * phCard [out] A handle that identifies the connection to the smart card in
	 * the designated reader.
	 * 
	 * pdwActiveProtocol [out] A flag that indicates the established active
	 * protocol. Value Meaning SCARD_PROTOCOL_T0 T=0 is the active protocol.
	 * SCARD_PROTOCOL_T1 T=1 is the active protocol. SCARD_PROTOCOL_UNDEFINED
	 * SCARD_SHARE_DIRECT has been specified, so that no protocol negotiation
	 * has occurred. It is possible that there is no card in the reader.
	 * 
	 * @return SCARDHANDLE
	 */
	public abstract int SCardConnect(final SCARDCONTEXT hContext,
			final NativeString szReader, final long dwShareMode,
			final long dwPreferredProtocols, final NativeLongLP64 phCard,
			final NativePcscDword pdwActiveProtocol);

	/**
	 * The SCardControl function gives you direct control of the reader. You can
	 * call it any time after a successful call to SCardConnect and before a
	 * successful call to SCardDisconnect. The effect on the state of the reader
	 * depends on the control code.
	 * 
	 * <pre>
	 * LONG SCardControl(SCARDHANDLE hCard, DWORD dwControlCode, LPCVOID lpInBuffer,
	 * 		DWORD nInBufferSize, LPVOID lpOutBuffer, DWORD nOutBufferSize,
	 * 		LPDWORD lpBytesReturned);
	 * </pre>
	 */
	public abstract int SCardControl(final SCARDHANDLE hCard,
			final long dwControlCode, final NativeBuffer lpInBuffer,
			final long nInBufferSize, final NativeBuffer lpOutBuffer,
			final long nOutBufferSize, final NativePcscDword lpBytesReturned);

	/**
	 * The SCardDisconnect function terminates a connection previously opened
	 * between the calling application and a smart card in the target reader.
	 * 
	 * LONG SCardDisconnect( SCARDHANDLE hCard, DWORD dwDisposition );
	 * 
	 * Parameters
	 * 
	 * hCard [in] Reference value obtained from a previous call to SCardConnect.
	 * 
	 * dwDisposition [in] Action to take on the card in the connected reader on
	 * close. Value Meaning SCARD_LEAVE_CARD Do not do anything special.
	 * SCARD_RESET_CARD Reset the card. SCARD_UNPOWER_CARD Power down the card.
	 * SCARD_EJECT_CARD Eject the card.
	 * 
	 */
	public abstract int SCardDisconnect(final SCARDHANDLE hCard,
			final long dwDisposition);

	/**
	 * The SCardEndTransaction function completes a previously declared
	 * transaction, allowing other applications to resume interactions with the
	 * card.
	 * 
	 * <pre>
	 * LONG SCardEndTransaction(
	 * 	__in SCARDHANDLE hCard,
	 * 	__in DWORD dwDisposition
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * hCard [in]
	 * 
	 * Reference value obtained from a previous call to SCardConnect. This value
	 * would also have been used in an earlier call to SCardBeginTransaction.
	 * dwDisposition [in]
	 * 
	 * Action to take on the card in the connected reader on close.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_LEAVE_CARD</td>
	 * <td>Do not do anything special.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_RESET_CARD</td>
	 * <td>Reset the card.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_UNPOWER_CARD</td>
	 * <td>Power down the card.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_EJECT_CARD</td>
	 * <td>Eject the card.</td>
	 * </tr>
	 * </table>
	 */
	public abstract int SCardEndTransaction(final SCARDHANDLE hCard,
			final long dwDisposition);

	/**
	 * The SCardEstablishContext function establishes the resource manager
	 * context (the scope) within which database operations are performed.
	 * 
	 * LONG SCardEstablishContext( DWORD dwScope, LPCVOID pvReserved1, LPCVOID
	 * pvReserved2, LPSCARDCONTEXT phContext );
	 * 
	 * Parameters
	 * 
	 * <b>dwScope [in]</b> Scope of the resource manager context. This parameter
	 * can be one of the following values. Value Meaning SCARD_SCOPE_USER
	 * Database operations are performed within the domain of the user.
	 * SCARD_SCOPE_SYSTEM Database operations are performed within the domain of
	 * the system. The calling application must have appropriate access
	 * permissions for any database actions.<br />
	 * <b>pvReserved1 [in]</b> Reserved for future use and must be NULL. This
	 * parameter will allow a suitably privileged management application to act
	 * on behalf of another user.<br />
	 * <b>pvReserved2 [in]</b> Reserved for future use and must be NULL.<br />
	 * <b>phContext [out]</b> Handle to the established resource manager
	 * context. This handle can now be supplied to other functions attempting to
	 * do work within this context.
	 * 
	 * 
	 * 
	 * 
	 * @param scope
	 * @return If the function succeeds, the function returns SCARD_S_SUCCESS. <br/>
	 *         If the function fails, it returns an error code. For more
	 *         information, see Smart Card Return Values.
	 * @throws PCSCException
	 */
	public abstract int SCardEstablishContext(final long dwScope,
			final NativeLongLP64 phContext);

	/**
	 * <pre>
	 * LONG SCardGetAttrib(
	 * 			  __in     SCARDHANDLE hCard,
	 * 			  __in     DWORD dwAttrId,
	 * 			  __out    LPBYTE pbAttr,
	 * 			  __inout  LPDWORD pcbAttrLen
	 * 			);
	 * </pre>
	 */
	public int SCardGetAttrib(final SCARDHANDLE hCard, long dwAttrId,
			final NativeBuffer pbAttr, final NativePcscDword pcbAttrLength);

	/**
	 * The SCardGetStatusChange function blocks execution until the current
	 * availability of the cards in a specific set of readers changes.
	 * 
	 * The caller supplies a list of readers to be monitored by an
	 * SCARD_READERSTATE array and the maximum amount of time (in milliseconds)
	 * that it is willing to wait for an action to occur on one of the listed
	 * readers. Note that SCardGetStatusChange uses the user-supplied value in
	 * the dwCurrentState members of the rgReaderStates SCARD_READERSTATE array
	 * as the definition of the current state of the readers. The function
	 * returns when there is a change in availability, having filled in the
	 * dwEventState members of rgReaderStates appropriately.
	 * 
	 * LONG SCardGetStatusChange( SCARDCONTEXT hContext, DWORD dwTimeout,
	 * LPSCARD_READERSTATE rgReaderStates, DWORD cReaders );
	 * 
	 * Parameters
	 * 
	 * hContext [in] Handle that identifies the resource manager context. The
	 * resource manager context is set by a previous call to
	 * SCardEstablishContext.
	 * 
	 * dwTimeout [in] Maximum amount of time (in milliseconds) to wait for an
	 * action. A value of zero causes the function to return immediately. A
	 * value of INFINITE causes this function never to time out.
	 * 
	 * rgReaderStates [in, out] Array of SCARD_READERSTATE structures that
	 * specify the readers to watch, and receives the result.
	 * 
	 * cReaders [in] Number of elements in the rgReaderStates array.
	 */
	public abstract int SCardGetStatusChange(final SCARDCONTEXT hContext,
			final long dwTimeout, final INativeObject rgReaderStates,
			final long cReaders);

	/**
	 * The SCardListReaders function provides the list of readers within a set
	 * of named reader groups, eliminating duplicates.
	 * 
	 * The caller supplies a list of reader groups, and receives the list of
	 * readers within the named groups. Unrecognized group names are ignored.
	 * 
	 * LONG SCardListReaders( SCARDCONTEXT hContext, LPCTSTR mszGroups, LPTSTR
	 * mszReaders, LPDWORD pcchReaders );
	 * 
	 * Parameters
	 * 
	 * hContext [in] Handle that identifies the resource manager context for the
	 * query. The resource manager context can be set by a previous call to
	 * SCardEstablishContext. This parameter cannot be NULL.
	 * 
	 * mszGroups [in] Names of the reader groups defined to the system, as a
	 * multi-string. Use a NULL value to list all readers in the system (that
	 * is, the SCard$AllReaders group).
	 * 
	 * mszReaders [out] Multi-string that lists the card readers within the
	 * supplied reader groups. If this value is NULL, SCardListReaders ignores
	 * the buffer length supplied in pcchReaders, writes the length of the
	 * buffer that would have been returned if this parameter had not been NULL
	 * to pcchReaders, and returns a success code.
	 * 
	 * pcchReaders [in, out] Length of the mszReaders buffer in characters. This
	 * parameter receives the actual length of the multi-string structure,
	 * including all trailing null characters. If the buffer length is specified
	 * as SCARD_AUTOALLOCATE, then mszReaders is converted to a pointer to a
	 * byte pointer, and receives the address of a block of memory containing
	 * the multi-string structure. This block of memory must be deallocated with
	 * SCardFreeMemory.
	 * 
	 * Return Values
	 * 
	 * This function returns different values depending on whether it succeeds
	 * or fails. Return code Description Success SCARD_S_SUCCESS. Group contains
	 * no readers SCARD_E_NO_READERS_AVAILABLE Other An error code. For more
	 * information, see Smart Card Return Values.
	 * 
	 * @throws PCSCException
	 */
	public abstract int SCardListReaders(final SCARDCONTEXT hContext,
			final NativeString mszGroups, final INativeObject mszReaders,
			final NativePcscDword pcchReaders);

	/**
	 * The SCardReconnect function reestablishes an existing connection between
	 * the calling application and a smart card. This function moves a card
	 * handle from direct access to general access, or acknowledges and clears
	 * an error condition that is preventing further access to the card.
	 * 
	 * LONG WINAPI SCardReconnect( __in SCARDHANDLE hCard, __in DWORD
	 * dwShareMode, __in DWORD dwPreferredProtocols, __in DWORD
	 * dwInitialization, __out_opt LPDWORD pdwActiveProtocol );
	 * 
	 * Parameters
	 * 
	 * hCard [in]
	 * 
	 * Reference value obtained from a previous call to SCardConnect.
	 * dwShareMode [in]
	 * 
	 * Flag that indicates whether other applications may form connections to
	 * this card. Value Meaning
	 * 
	 * SCARD_SHARE_SHARED
	 * 
	 * 
	 * 
	 * This application will share this card with other applications.
	 * 
	 * SCARD_SHARE_EXCLUSIVE
	 * 
	 * 
	 * 
	 * This application will not share this card with other applications.
	 * 
	 * 
	 * dwPreferredProtocols [in]
	 * 
	 * Bitmask of acceptable protocols for this connection. Possible values may
	 * be combined with the OR operation. The value of this parameter should
	 * include the current protocol. Attempting to reconnect with a protocol
	 * other than the current protocol will result in an error. Value Meaning
	 * 
	 * SCARD_PROTOCOL_T0
	 * 
	 * 
	 * 
	 * T=0 is an acceptable protocol.
	 * 
	 * SCARD_PROTOCOL_T1
	 * 
	 * 
	 * 
	 * T=1 is an acceptable protocol.
	 * 
	 * 
	 * dwInitialization [in]
	 * 
	 * Type of initialization that should be performed on the card. Value
	 * Meaning
	 * 
	 * SCARD_LEAVE_CARD
	 * 
	 * 
	 * 
	 * Do not do anything special on reconnect.
	 * 
	 * SCARD_RESET_CARD
	 * 
	 * 
	 * 
	 * Reset the card (Warm Reset).
	 * 
	 * SCARD_UNPOWER_CARD
	 * 
	 * 
	 * 
	 * Power down the card and reset it (Cold Reset).
	 * 
	 * 
	 * pdwActiveProtocol [out, optional]
	 * 
	 * Flag that indicates the established active protocol. Value Meaning
	 * 
	 * SCARD_PROTOCOL_T0
	 * 
	 * 
	 * 
	 * T=0 is the active protocol.
	 * 
	 * SCARD_PROTOCOL_T1
	 * 
	 * 
	 * 
	 * T=1 is the active protocol.
	 * 
	 * @return SCARDHANDLE
	 */
	public abstract int SCardReconnect(final SCARDHANDLE hCard,
			final long dwShareMode, final long dwPreferredProtocols,
			final long dwInitialization, final NativePcscDword pdwActiveProtocol);

	/**
	 * The SCardReleaseContext function closes an established resource manager
	 * context, freeing any resources allocated under that context, including
	 * SCARDHANDLE objects and memory allocated using the SCARD_AUTOALLOCATE
	 * length designator.
	 * 
	 * LONG SCardReleaseContext( SCARDCONTEXT hContext );
	 * 
	 * Parameters
	 * 
	 * hContext [in] Handle that identifies the resource manager context. The
	 * resource manager context is set by a previous call to
	 * SCardEstablishContext.
	 * 
	 * Return Values
	 * 
	 * This function returns different values depending on whether it succeeds
	 * or fails. Return code Description Success SCARD_S_SUCCESS. Failure An
	 * error code. For more information, see Smart Card Return Values.
	 * 
	 * @throws PCSCException
	 * 
	 */
	public abstract int SCardReleaseContext(final SCARDCONTEXT hContext);

	/**
	 * Returns the current status of the reader connected to by hCard.
	 * 
	 * It's friendly name will be stored in szReaderName. pcchReaderLen will be
	 * the size of the allocated buffer for szReaderName, while pcbAtrLen will
	 * be the size of the allocated buffer for pbAtr. If either of these is too
	 * small, the function will return with SCARD_E_INSUFFICIENT_BUFFER and the
	 * necessary size in pcchReaderLen and pcbAtrLen. The current state, and
	 * protocol will be stored in pdwState and pdwProtocol respectively.
	 * 
	 * pdwState also contains a number of events in the upper 16 bits (*pdwState
	 * & 0xFFFF0000). This number of events is incremented for each card
	 * insertion or removal in the specified reader. This can be used to detect
	 * a card removal/insertion between two calls to SCardStatus().
	 * 
	 * If *pcchReaderLen is equal to SCARD_AUTOALLOCATE then the function will
	 * allocate itself the needed memory for mszReaderName. Use
	 * SCardFreeMemory() to release it.
	 * 
	 * If *pcbAtrLen is equal to SCARD_AUTOALLOCATE then the function will
	 * allocate itself the needed memory for pbAtr. Use SCardFreeMemory() to
	 * release it.
	 * 
	 * @param hCard
	 * @param szReaderName
	 * @param pcchReaderLen
	 * @param pdwState
	 * @param pdwProtocol
	 * @param pbAtr
	 * @param pcbAtrLen
	 * @return
	 */
	public int SCardStatus(final SCARDHANDLE hCard,
			final INativeObject szReaderName,
			final NativePcscDword pcchReaderLen,
			final NativePcscDword pdwState, final NativePcscDword pdwProtocol,
			final NativeBuffer pbAtr, final NativePcscDword pcbAtrLen);

	/**
	 * The SCardTransmit function sends a service request to the smart card and
	 * expects to receive data back from the card.
	 * 
	 * LONG SCardTransmit( SCARDHANDLE hCard, LPCSCARD_I0_REQUEST pioSendPci,
	 * LPCBYTE pbSendBuffer, DWORD cbSendLength, LPSCARD_IO_REQUEST pioRecvPci,
	 * LPBYTE pbRecvBuffer, LPDWORD pcbRecvLength );
	 * 
	 * Parameters
	 * 
	 * hCard [in] A reference value returned from the SCardConnect function.
	 * 
	 * pioSendPci [in] A pointer to the protocol header structure for the
	 * instruction. This buffer is in the format of an SCARD_IO_REQUEST
	 * structure, followed by the specific protocol control information (PCI).
	 * 
	 * For the T=0, T=1, and Raw protocols, the PCI structure is constant. The
	 * smart card subsystem supplies a global T=0, T=1, or Raw PCI structure,
	 * which you can reference by using the symbols SCARD_PCI_T0, SCARD_PCI_T1,
	 * and SCARD_PCI_RAW respectively.
	 * 
	 * pbSendBuffer [in] A pointer to the actual data to be written to the card.
	 * 
	 * For T=0, the data parameters are placed into the address pointed to by
	 * pbSendBuffer according to the following structure:
	 * 
	 * struct { BYTE bCla, // the instruction class bIns, // the instruction
	 * code bP1, // parameter to the instruction bP2, // parameter to the
	 * instruction bP3; // size of I/O transfer } CmdBytes;
	 * 
	 * Members
	 * 
	 * The data sent to the card should immediately follow the send buffer. In
	 * the special case where no data is sent to the card and no data is
	 * expected in return, bP3 is not sent. Value Meaning bCla The T=0
	 * instruction class. bIns An instruction code in the T=0 instruction class.
	 * bP1, bP2 Reference codes that complete the instruction code. bP3 The
	 * number of data bytes to be transmitted during the command, per ISO
	 * 7816-4, Section 8.2.1.
	 * 
	 * cbSendLength [in] The length, in bytes, of the pbSendBuffer parameter.
	 * 
	 * For T=0, in the special case where no data is sent to the card and no
	 * data expected in return, this length must reflect that the bP3 member is
	 * not being sent; the length should be sizeof(CmdBytes) � sizeof(BYTE).
	 * 
	 * pioRecvPci [in, out] Pointer to the protocol header structure for the
	 * instruction, followed by a buffer in which to receive any returned
	 * protocol control information (PCI) specific to the protocol in use. This
	 * parameter can be NULL if no PCI is returned.
	 * 
	 * pbRecvBuffer [out] Pointer to any data returned from the card.
	 * 
	 * For T=0, the data is immediately followed by the SW1 and SW2 status
	 * bytes. If no data is returned from the card, then this buffer will only
	 * contain the SW1 and SW2 status bytes.
	 * 
	 * pcbRecvLength [in, out] Supplies the length, in bytes, of the
	 * pbRecvBuffer parameter and receives the actual number of bytes received
	 * from the smart card. This value cannot be SCARD_AUTOALLOCATE because
	 * SCardTransmit does not support SCARD_AUTOALLOCATE.
	 * 
	 * For T=0, the receive buffer must be at least two bytes long to receive
	 * the SW1 and SW2 status bytes.
	 * 
	 * @throws PCSCException
	 * 
	 */
	public abstract int SCardTransmit(final SCARDHANDLE hCard,
			final INativeHandle pioSendPci, final INativeObject pbSendBuffer,
			final long cbSendLength, final INativeObject pioRecvPci,
			final NativeBuffer pbRecvBuffer, final NativePcscDword pcbRecvLength);

}