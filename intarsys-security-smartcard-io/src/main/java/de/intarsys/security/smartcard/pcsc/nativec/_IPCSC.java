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
package de.intarsys.security.smartcard.pcsc.nativec;

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.INativeObject;
import de.intarsys.nativec.type.NativeBuffer;
import de.intarsys.nativec.type.NativeLongLP64;
import de.intarsys.nativec.type.NativeString;
import de.intarsys.nativec.type.NativeVoid;
import de.intarsys.security.smartcard.pcsc.PCSCException;
import de.intarsys.tools.system.SystemTools;

/**
 * This interface mimics the PCSC-Lite/Winscard API.
 * 
 */
public interface _IPCSC extends _PCSC_RETURN_CODES {

	static int CM_IOCTL_GET_FEATURE_REQUEST = 3400;

	static int FEATURE_ABORT = 0x0B;

	static int FEATURE_GET_KEY_PRESSED = 0x05;

	static int FEATURE_IFD_PIN_PROP = 0x0A;

	static int FEATURE_MCT_READERDIRECT = 0x08;

	static int FEATURE_MCT_UNIVERSAL = 0x09;

	static int FEATURE_MODIFY_PIN_DIRECT = 0x07;

	static int FEATURE_MODIFY_PIN_FINISH = 0x04;

	static int FEATURE_MODIFY_PIN_START = 0x03;

	static int FEATURE_VERIFY_PIN_DIRECT = 0x06;

	static int FEATURE_VERIFY_PIN_FINISH = 0x02;

	static int FEATURE_VERIFY_PIN_START = 0x01;

	static int FEATURE_EXECUTE_PACE = 0x20;

	/** Eject the card on close */
	static int SCARD_EJECT_CARD = 3;

	/** Don't do anything special on close */
	static int SCARD_LEAVE_CARD = 0;

	/** Raw is the active protocol. */
	static int SCARD_PROTOCOL_RAW = 0x00010000;

	/** T=0 is the active protocol. */
	static int SCARD_PROTOCOL_T0 = 0x00000001;

	/** T=1 is the active protocol. */
	static int SCARD_PROTOCOL_T1 = 0x00000002;

	/** This is the mask of ISO defined transmission protocols */
	static int SCARD_PROTOCOL_Tx = SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1;

	/** There is no active protocol. */
	static int SCARD_PROTOCOL_UNDEFINED = 0x00000000;

	/** Reset the card on close */
	static int SCARD_RESET_CARD = 1;

	static int SCARD_AUTOALLOCATE = -1;

	/**
	 * The context is the system context, and any database operations are
	 * performed within the domain of the system. (The calling application must
	 * have appropriate access permissions for any database actions.)
	 */
	static int SCARD_SCOPE_SYSTEM = 2;

	/**
	 * The context is that of the current terminal, and any database operations
	 * are performed within the domain of that terminal. (The calling
	 * application must have appropriate access permissions for any database
	 * actions.)
	 */
	static int SCARD_SCOPE_TERMINAL = 1;

	/**
	 * The context is a user context, and any database operations are performed
	 * within the domain of the user.
	 */
	static int SCARD_SCOPE_USER = 0;

	/**
	 * This application demands direct control of the reader, so it is not
	 * available to other applications.
	 */
	static int SCARD_SHARE_DIRECT = 3;

	/**
	 * This application is not willing to share this card with other
	 * applications.
	 */
	static int SCARD_SHARE_EXCLUSIVE = 1;

	/**
	 * This application is willing to share this card with other applications.
	 */
	static int SCARD_SHARE_SHARED = 2;

	/**
	 * This implies that there is a card in the reader with an ATR matching one
	 * of the target cards. If this bit is set, SCARD_STATE_PRESENT will also be
	 * set. This bit is only returned on the SCardLocateCard() service.
	 */

	static int SCARD_STATE_ATRMATCH = 0x00000040;

	/**
	 * This implies that there is a difference between the state believed by the
	 * application, and the state known by the Service Manager. When this bit is
	 * set, the application may assume a significant state change has occurred
	 * on this reader.
	 */
	static int SCARD_STATE_CHANGED = 0x00000002;

	/**
	 * This implies that there is not card in the reader. If this bit is set,
	 * all the following bits will be clear.
	 */
	static int SCARD_STATE_EMPTY = 0x00000010;
	/**
	 * This implies that the card in the reader is allocated for exclusive use
	 * by another application. If this bit is set, SCARD_STATE_PRESENT will also
	 * be set.
	 */
	static int SCARD_STATE_EXCLUSIVE = 0x00000080;
	/**
	 * The application requested that this reader be ignored. No other bits will
	 * be set.
	 */
	static int SCARD_STATE_IGNORE = 0x00000001;
	/**
	 * This implies that the card in the reader is in use by one or more other
	 * applications, but may be connected to in shared mode. If this bit is set,
	 * SCARD_STATE_PRESENT will also be set.
	 */
	static int SCARD_STATE_INUSE = 0x00000100;
	/**
	 * This implies that the card in the reader is unresponsive or not supported
	 * by the reader or software.
	 */
	static int SCARD_STATE_MUTE = 0x00000200;
	/**
	 * This implies that there is a card in the reader.
	 */
	static int SCARD_STATE_PRESENT = 0x00000020;
	/**
	 * This implies that the actual state of this reader is not available. If
	 * this bit is set, then all the following bits are clear.
	 */
	static int SCARD_STATE_UNAVAILABLE = 0x00000008;
	/**
	 * The application is unaware of the current state, and would like to know.
	 * The use of this value results in an immediate return from state
	 * transition monitoring services. This is represented by all bits set to
	 * zero.
	 */
	static int SCARD_STATE_UNAWARE = 0x00000000;
	/**
	 * This implies that the given reader name is not recognized by the Service
	 * Manager. If this bit is set, then SCARD_STATE_CHANGED and
	 * SCARD_STATE_IGNORE will also be set.
	 */
	static int SCARD_STATE_UNKNOWN = 0x00000004;
	/**
	 * This implies that the card in the reader has not been powered up.
	 */
	static int SCARD_STATE_UNPOWERED = 0x00000400;

	/** Power down the card on close */
	static int SCARD_UNPOWER_CARD = 2;

	static boolean SupportsAutoallocate = !SystemTools.isMac();
	static boolean SupportsProtocolUndefined = !SystemTools.isMac();

	/**
	 * Get the reference to the global structure SCARD_PCI_RAW
	 * 
	 * @return constant pointer to global structure SCARD_PCI_T0
	 */
	INativeHandle getSCARD_PCI_RAW();

	/**
	 * Get the reference to the global structure SCARD_PCI_T0
	 * 
	 * @return constant pointer to global structure SCARD_PCI_T0
	 */
	INativeHandle getSCARD_PCI_T0();

	/**
	 * Get the reference to the global structure SCARD_PCI_T1
	 * 
	 * @return constant pointer to global structure SCARD_PCI_T1
	 */
	INativeHandle getSCARD_PCI_T1();

	/**
	 * The SCardBeginTransaction function starts a transaction.
	 * 
	 * The function waits for the completion of all other transactions before it
	 * begins. After the transaction starts, all other applications are blocked
	 * from accessing the smart card while the transaction is in progress.
	 * 
	 * <pre>
	 * LONG SCardBeginTransaction(
	 *   _In_ SCARDHANDLE hCard
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hCard [in]</dt>
	 * 
	 * <dd>A reference value obtained from a previous call to SCardConnect.
	 * <dd>
	 * </dl>
	 */
	int SCardBeginTransaction(final SCARDHANDLE hCard);

	/**
	 * The SCardCancel function terminates all outstanding actions within a
	 * specific resource manager context.
	 * 
	 * The only requests that you can cancel are those that require waiting for
	 * external action by the smart card or user. Any such outstanding action
	 * requests will terminate with a status indication that the action was
	 * canceled. This is especially useful to force outstanding
	 * SCardGetStatusChange calls to terminate.
	 * 
	 * <pre>
	 * LONG SCardCancel(
	 *   _In_ SCARDCONTEXT hContext
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hContext [in]</dt>
	 * <dd>Handle that identifies the resource manager context. The resource
	 * manager context is set by a previous call to SCardEstablishContext.</dd>
	 * </dl>
	 */
	int SCardCancel(final SCARDCONTEXT hContext);

	/**
	 * The SCardConnect function establishes a connection (using a specific
	 * resource manager context) between the calling application and a smart
	 * card contained by a specific reader. If no card exists in the specified
	 * reader, an error is returned.
	 * 
	 * <pre>
	 * LONG SCardConnect(
	 *   _In_  SCARDCONTEXT  hContext,
	 *   _In_  LPCTSTR       szReader,
	 *   _In_  DWORD         dwShareMode,
	 *   _In_  DWORD         dwPreferredProtocols,
	 *   _Out_ LPSCARDHANDLE phCard,
	 *   _Out_ LPDWORD       pdwActiveProtocol
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hContext [in]</dt>
	 * <dd>A handle that identifies the resource manager context. The resource
	 * manager context is set by a previous call to SCardEstablishContext.
	 * <dd>
	 * <dt>szReader [in]</dt>
	 * <dd>The name of the reader that contains the target card.</dd>
	 * <dt>dwShareMode [in]</dt>
	 * <dd>A flag that indicates whether other applications may form connections
	 * to the card.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SHARE_SHARED</td>
	 * <td>This application is willing to share the card with other
	 * applications.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SHARE_EXCLUSIVE</td>
	 * <td>This application is not willing to share the card with other
	 * applications.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SHARE_DIRECT</td>
	 * <td>This application is allocating the reader for its private use, and
	 * will be controlling it directly. No other applications are allowed access
	 * to it.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>dwPreferredProtocols [in]</dt>
	 * <dd>A bitmask of acceptable protocols for the connection. Possible values
	 * may be combined with the OR operation.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T0</td>
	 * <td>T=0 is an acceptable protocol.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T1</td>
	 * <td>T=1 is an acceptable protocol.</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>This parameter may be zero only if dwShareMode is set to
	 * SCARD_SHARE_DIRECT. In this case, no protocol negotiation will be
	 * performed by the drivers until an IOCTL_SMARTCARD_SET_PROTOCOL control
	 * directive is sent with SCardControl.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>phCard [out]</dt>
	 * <dd>A handle that identifies the connection to the smart card in the
	 * designated reader.</dd>
	 * <dt>pdwActiveProtocol [out]</dt>
	 * <dd>A flag that indicates the established active protocol.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T0</td>
	 * <td>T=0 is the active protocol.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T1</td>
	 * <td>T=1 is the active protocol.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_UNDEFINED</td>
	 * <td>SCARD_SHARE_DIRECT has been specified, so that no protocol
	 * negotiation has occurred. It is possible that there is no card in the
	 * reader.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * </dl>
	 */
	int SCardConnect(final SCARDCONTEXT hContext, final NativeString szReader, final long dwShareMode,
			final long dwPreferredProtocols, final NativeLongLP64 phCard, final NativePcscDword pdwActiveProtocol);

	/**
	 * The SCardControl function gives you direct control of the reader. You can
	 * call it any time after a successful call to SCardConnect and before a
	 * successful call to SCardDisconnect. The effect on the state of the reader
	 * depends on the control code.
	 * 
	 * <pre>
	 * LONG SCardControl(
	 *   _In_  SCARDHANDLE hCard,
	 *   _In_  DWORD       dwControlCode,
	 *   _In_  LPCVOID     lpInBuffer,
	 *   _In_  DWORD       nInBufferSize,
	 *   _Out_ LPVOID      lpOutBuffer,
	 *   _In_  DWORD       nOutBufferSize,
	 *   _Out_ LPDWORD     lpBytesReturned
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hCard [in]</dt>
	 * <dd>Reference value returned from SCardConnect.</dd>
	 * <dt>dwControlCode [in]</dt>
	 * <dd>Control code for the operation. This value identifies the specific
	 * operation to be performed.</dd>
	 * <dt>lpInBuffer [in]</dt>
	 * <dd>Pointer to a buffer that contains the data required to perform the
	 * operation. This parameter can be NULL if the dwControlCode parameter
	 * specifies an operation that does not require input data.</dd>
	 * <dt>nInBufferSize [in]</dt>
	 * <dd>Size, in bytes, of the buffer pointed to by lpInBuffer.</dt>
	 * <dd>lpOutBuffer [out]</dt>
	 * <dd>Pointer to a buffer that receives the operation's output data. This
	 * parameter can be NULL if the dwControlCode parameter specifies an
	 * operation that does not produce output data.</dd>
	 * <dt>nOutBufferSize [in]</dt>
	 * <dd>Size, in bytes, of the buffer pointed to by lpOutBuffer.</dd>
	 * <dt>lpBytesReturned [out]</dt>
	 * <dd>Pointer to a DWORD that receives the size, in bytes, of the data
	 * stored into the buffer pointed to by lpOutBuffer.</dd>
	 * </dl>
	 */
	int SCardControl(final SCARDHANDLE hCard, final long dwControlCode, final NativeBuffer lpInBuffer,
			final long nInBufferSize, final NativeBuffer lpOutBuffer, final long nOutBufferSize,
			final NativePcscDword lpBytesReturned);

	/**
	 * The SCardDisconnect function terminates a connection previously opened
	 * between the calling application and a smart card in the target reader.
	 * 
	 * <pre>
	 * LONG SCardDisconnect(
	 *   _In_ SCARDHANDLE hCard,
	 *   _In_ DWORD       dwDisposition
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hCard [in]</dt>
	 * <dd>Reference value obtained from a previous call to SCardConnect.</dd>
	 * <dt>dwDisposition [in]</dt>
	 * <dd>Action to take on the card in the connected reader on close.
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
	 * </dd>
	 * </dl>
	 */
	int SCardDisconnect(final SCARDHANDLE hCard, final long dwDisposition);

	/**
	 * The SCardEndTransaction function completes a previously declared
	 * transaction, allowing other applications to resume interactions with the
	 * card.
	 * 
	 * <pre>
	 * LONG SCardEndTransaction(
	 *   _In_ SCARDHANDLE hCard,
	 *   _In_ DWORD       dwDisposition
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hCard [in]</dt>
	 * <dd>Reference value obtained from a previous call to SCardConnect. This
	 * value would also have been used in an earlier call to
	 * SCardBeginTransaction.</dd>
	 * <dt>dwDisposition [in]</dt>
	 * <dd>Action to take on the card in the connected reader on close.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_EJECT_CARD</td>
	 * <td>Eject the card.</td>
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
	 * </table>
	 * </dd>
	 * </dl>
	 */
	int SCardEndTransaction(final SCARDHANDLE hCard, final long dwDisposition);

	/**
	 * The SCardEstablishContext function establishes the resource manager
	 * context (the scope) within which database operations are performed.
	 * 
	 * <pre>
	 * LONG SCardEstablishContext(
	 *   _In_  DWORD          dwScope,
	 *   _In_  LPCVOID        pvReserved1,
	 *   _In_  LPCVOID        pvReserved2,
	 *   _Out_ LPSCARDCONTEXT phContext
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>dwScope [in]</dt>
	 * <dd>Scope of the resource manager context. This parameter can be one of
	 * the following values.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SCOPE_USER</td>
	 * <td>Database operations are performed within the domain of the user.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SCOPE_SYSTEM</td>
	 * <td>Database operations are performed within the domain of the system.
	 * The calling application must have appropriate access permissions for any
	 * database actions.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>pvReserved1 [in]</dt>
	 * <dd>Reserved for future use and must be NULL. This parameter will allow a
	 * suitably privileged management application to act on behalf of another
	 * user.</dd>
	 * <dt>pvReserved2 [in]</dt>
	 * <dd>Reserved for future use and must be NULL.</dd>
	 * <dt>phContext [out]</dt>
	 * <dd>A handle to the established resource manager context. This handle can
	 * now be supplied to other functions attempting to do work within this
	 * context.</dd>
	 * </dl>
	 * 
	 * @param dwScope
	 * @param phContext
	 * @return If the function succeeds, the function returns SCARD_S_SUCCESS.
	 *         <br />
	 *         If the function fails, it returns an error code. For more
	 *         information, see Smart Card Return Values.
	 * @throws PCSCException
	 */
	int SCardEstablishContext(final long dwScope, final NativeLongLP64 phContext);

	/**
	 * The SCardFreeMemory function releases memory that has been returned from
	 * the resource manager using the SCARD_AUTOALLOCATE length designator.
	 * 
	 * <pre>
	 * LONG SCardFreeMemory(
	 *   _In_ SCARDCONTEXT hContext,
	 *   _In_ LPCVOID      pvMem
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hContext [in]</dt>
	 * <dd>Handle that identifies the resource manager context returned from
	 * SCardEstablishContext, or NULL if the creating function also specified
	 * NULL for its hContext parameter. For more information, see Smart Card
	 * Database Query Functions.</dd>
	 * <dt>pvMem [in]</dt>
	 * <dd>Memory block to be released.</dt>
	 * </dl>
	 */
	int SCardFreeMemory(SCARDCONTEXT hContext, NativeVoid pvMem);

	/**
	 * The SCardGetAttrib function retrieves the current reader attributes for
	 * the given handle. It does not affect the state of the reader, driver, or
	 * card.
	 * 
	 * <pre>
	 * LONG SCardGetAttrib(
	 *   _In_    SCARDHANDLE hCard,
	 *   _In_    DWORD       dwAttrId,
	 *   _Out_   LPBYTE      pbAttr,
	 *   _Inout_ LPDWORD     pcbAttrLen
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hCard [in]</dt>
	 * <dd>Reference value returned from {@link #SCardConnect}.</dd>
	 * <dt>dwAttrId [in]</dt>
	 * <dd>Identifier for the attribute to get. The following table lists
	 * possible values for dwAttrId. These values are read-only. Note that
	 * vendors may not support all attributes.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_ATR_STRING</td>
	 * <td>Answer to reset (ATR) string.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CHANNEL_ID</td>
	 * <td>DWORD encoded as 0xDDDDCCCC, where DDDD = data channel type and CCCC
	 * = channel number:<br />
	 * The following encodings are defined for DDDD:
	 * <ul>
	 * <li>0x01 serial I/O; CCCC is a port number.</li>
	 * <li>0x02 parallel I/O; CCCC is a port number.</li>
	 * <li>0x04 PS/2 keyboard port; CCCC is zero.</li>
	 * <li>0x08 SCSI; CCCC is SCSI ID number.</li>
	 * <li>0x10 IDE; CCCC is device number.</li>
	 * <li>0x20 USB; CCCC is device number.</li>
	 * <li>0xFy vendor-defined interface with y in the range zero through 15;
	 * CCCC is vendor defined.</li>
	 * </ul>
	 * </td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CHARACTERISTICS</td>
	 * <td>DWORD indicating which mechanical characteristics are supported. If
	 * zero, no special characteristics are supported. Note that multiple bits
	 * can be set:
	 * <ul>
	 * <li>0x00000001 Card swallowing mechanism</li>
	 * <li>0x00000002 Card ejection mechanism</li>
	 * <li>0x00000004 Card capture mechanism</li>
	 * </ul>
	 * All other values are reserved for future use (RFU).</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_BWT</td>
	 * <td>Current block waiting time.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_CLK</td>
	 * <td>Current clock rate, in kHz.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_CWT</td>
	 * <td>Current character waiting time.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_D</td>
	 * <td>Bit rate conversion factor.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_EBC_ENCODING</td>
	 * <td>Current error block control encoding.<br />
	 * 0 = longitudinal redundancy check (LRC)<br />
	 * 1 = cyclical redundancy check (CRC)</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_F</td>
	 * <td>Clock conversion factor.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_IFSC</td>
	 * <td>Current byte size for information field size card.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_IFSD</td>
	 * <td>Current byte size for information field size device.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_N</td>
	 * <td>Current guard time.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_PROTOCOL_TYPE</td>
	 * <td>DWORD encoded as 0x0rrrpppp where rrr is RFU and should be 0x000.
	 * pppp encodes the current protocol type. Whichever bit has been set
	 * indicates which ISO protocol is currently in use. (For example, if bit
	 * zero is set, T=0 protocol is in effect.)</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_CURRENT_W</td>
	 * <td>Current work waiting time.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_DEFAULT_CLK</td>
	 * <td>Default clock rate, in kHz.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_DEFAULT_DATA_RATE</td>
	 * <td>Default data rate, in bps.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_DEVICE_FRIENDLY_NAME</td>
	 * <td>Reader's display name.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_DEVICE_IN_USE</td>
	 * <td>Reserved for future use.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_DEVICE_SYSTEM_NAME</td>
	 * <td>Reader's system name.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_DEVICE_UNIT</td>
	 * <td>Instance of this vendor's reader attached to the computer. The first
	 * instance will be device unit 0, the next will be unit 1 (if it is the
	 * same brand of reader) and so on. Two different brands of readers will
	 * both have zero for this value.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_ICC_INTERFACE_STATUS</td>
	 * <td>Single byte. Zero if smart card electrical contact is not active;
	 * nonzero if contact is active.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_ICC_PRESENCE</td>
	 * <td>Single byte indicating smart card presence:<br />
	 * 0 = not present<br />
	 * 1 = card present but not swallowed (applies only if reader supports smart
	 * card swallowing)<br />
	 * 2 = card present (and swallowed if reader supports smart card swallowing)
	 * <br />
	 * 4 = card confiscated.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_ICC_TYPE_PER_ATR</td>
	 * <td>Single byte indicating smart card type:<br />
	 * 0 = unknown type<br />
	 * 1 = 7816 Asynchronous<br />
	 * 2 = 7816 Synchronous<br />
	 * Other values RFU.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_MAX_CLK</td>
	 * <td>Maximum clock rate, in kHz.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_MAX_DATA_RATE</td>
	 * <td>Maximum data rate, in bps.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_MAX_IFSD</td>
	 * <td>Maximum bytes for information file size device.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_POWER_MGMT_SUPPORT</td>
	 * <td>Zero if device does not support power down while smart card is
	 * inserted. Nonzero otherwise.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_PROTOCOL_TYPES</td>
	 * <td>DWORD encoded as 0x0rrrpppp where rrr is RFU and should be 0x000.
	 * pppp encodes the supported protocol types. A '1' in a given bit position
	 * indicates support for the associated ISO protocol, so if bits zero and
	 * one are set, both T=0 and T=1 protocols are supported.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_VENDOR_IFD_SERIAL_NO</td>
	 * <td>Vendor-supplied interface device serial number.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_VENDOR_IFD_TYPE</td>
	 * <td>Vendor-supplied interface device type (model designation of reader).
	 * </td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_VENDOR_IFD_VERSION</td>
	 * <td>Vendor-supplied interface device version (DWORD in the form
	 * 0xMMmmbbbb where MM = major version, mm = minor version, and bbbb = build
	 * number).</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ATTR_VENDOR_NAME</td>
	 * <td>Vendor name.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>pbAttr [out]</dt>
	 * <dd>Pointer to a buffer that receives the attribute whose ID is supplied
	 * in dwAttrId. If this value is NULL, SCardGetAttrib ignores the buffer
	 * length supplied in pcbAttrLen, writes the length of the buffer that would
	 * have been returned if this parameter had not been NULL to pcbAttrLen, and
	 * returns a success code.</dd>
	 * <dt>pcbAttrLen [in, out]</dt>
	 * <dd>Length of the pbAttr buffer in bytes, and receives the actual length
	 * of the received attribute If the buffer length is specified as
	 * SCARD_AUTOALLOCATE, then pbAttr is converted to a pointer to a byte
	 * pointer, and receives the address of a block of memory containing the
	 * attribute. This block of memory must be deallocated with
	 * {@link #SCardFreeMemory}.
	 * <dd>
	 * </dl>
	 */
	int SCardGetAttrib(final SCARDHANDLE hCard, long dwAttrId, final NativeBuffer pbAttr,
			final NativePcscDword pcbAttrLength);

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
	 * <pre>
	 * LONG SCardGetStatusChange(
	 *   _In_    SCARDCONTEXT        hContext,
	 *   _In_    DWORD               dwTimeout,
	 *   _Inout_ LPSCARD_READERSTATE rgReaderStates,
	 *   _In_    DWORD               cReaders
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hContext [in]</dt>
	 * <dd>A handle that identifies the resource manager context. The resource
	 * manager context is set by a previous call to the SCardEstablishContext
	 * function.</dd>
	 * <dt>dwTimeout [in]</dt>
	 * <dd>The maximum amount of time, in milliseconds, to wait for an action. A
	 * value of zero causes the function to return immediately. A value of
	 * INFINITE causes this function never to time out.</dd>
	 * <dt>rgReaderStates [in, out]</dt>
	 * <dd>An array of SCARD_READERSTATE structures that specify the readers to
	 * watch, and that receives the result. To be notified of the arrival of a
	 * new smart card reader, set the szReader member of a SCARD_READERSTATE
	 * structure to "\\\\?PnP?\\Notification", and set all of the other members
	 * of that structure to zero. <div><em>Important</em> Each member of each
	 * structure in this array must be initialized to zero and then set to
	 * specific values as necessary. If this is not done, the function will fail
	 * in situations that involve remote card readers.</div></dd>
	 * <dt>cReaders [in]</dt>
	 * <dd>The number of elements in the rgReaderStates array.</dd>
	 * </dl>
	 */
	int SCardGetStatusChange(final SCARDCONTEXT hContext, final long dwTimeout, final INativeObject rgReaderStates,
			final long cReaders);

	/**
	 * The SCardListReaders function provides the list of readers within a set
	 * of named reader groups, eliminating duplicates.
	 * 
	 * The caller supplies a list of reader groups, and receives the list of
	 * readers within the named groups. Unrecognized group names are ignored.
	 * This function only returns readers within the named groups that are
	 * currently attached to the system and available for use.
	 * 
	 * <pre>
	 * LONG SCardListReaders(
	 *   _In_     SCARDCONTEXT hContext,
	 *   _In_opt_ LPCTSTR      mszGroups,
	 *   _Out_    LPTSTR       mszReaders,
	 *   _Inout_  LPDWORD      pcchReaders
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hContext [in]</dt>
	 * <dd>Handle that identifies the resource manager context for the query.
	 * The resource manager context can be set by a previous call to
	 * {@link #SCardEstablishContext}.<br />
	 * If this parameter is set to NULL, the search for readers is not limited
	 * to any context.</dd>
	 * <dt>mszGroups [in, optional]</dt>
	 * <dd>Names of the reader groups defined to the system, as a multi-string.
	 * Use a NULL value to list all readers in the system (that is, the
	 * SCard$AllReaders group).
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ALL_READERS<br />
	 * TEXT("SCard$AllReaders\000")</td>
	 * <td>Group used when no group name is provided when listing readers.
	 * Returns a list of all readers, regardless of what group or groups the
	 * readers are in.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_DEFAULT_READERS<br />
	 * TEXT("SCard$DefaultReaders\000")</td>
	 * <td>Default group to which all readers are added when introduced into the
	 * system.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_LOCAL_READERS<br />
	 * TEXT("SCard$LocalReaders\000")</td>
	 * <td>Unused legacy value. This is an internally managed group that cannot
	 * be modified by using any reader group APIs. It is intended to be used for
	 * enumeration only.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SYSTEM_READERS<br />
	 * TEXT("SCard$SystemReaders\000")</td>
	 * <td>Unused legacy value. This is an internally managed group that cannot
	 * be modified by using any reader group APIs. It is intended to be used for
	 * enumeration only.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>mszReaders [out]</dt>
	 * <dd>Multi-string that lists the card readers within the supplied reader
	 * groups. If this value is NULL, SCardListReaders ignores the buffer length
	 * supplied in pcchReaders, writes the length of the buffer that would have
	 * been returned if this parameter had not been NULL to pcchReaders, and
	 * returns a success code.</dd>
	 * <dt>pcchReaders [in, out]</dt>
	 * <dd>Length of the mszReaders buffer in characters. This parameter
	 * receives the actual length of the multi-string structure, including all
	 * trailing null characters. If the buffer length is specified as
	 * SCARD_AUTOALLOCATE, then mszReaders is converted to a pointer to a byte
	 * pointer, and receives the address of a block of memory containing the
	 * multi-string structure. This block of memory must be deallocated with
	 * SCardFreeMemory.</dd>
	 * </dl>
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
	int SCardListReaders(final SCARDCONTEXT hContext, final NativeString mszGroups, final INativeObject mszReaders,
			final NativePcscDword pcchReaders);

	/**
	 * The SCardReconnect function reestablishes an existing connection between
	 * the calling application and a smart card. This function moves a card
	 * handle from direct access to general access, or acknowledges and clears
	 * an error condition that is preventing further access to the card.
	 * 
	 * <pre>
	 * LONG SCardReconnect(
	 *   _In_      SCARDHANDLE hCard,
	 *   _In_      DWORD       dwShareMode,
	 *   _In_      DWORD       dwPreferredProtocols,
	 *   _In_      DWORD       dwInitialization,
	 *   _Out_opt_ LPDWORD     pdwActiveProtocol
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hCard [in]</dt>
	 * <dd>Reference value obtained from a previous call to
	 * {@link #SCardConnect}.</dd>
	 * <dt>dwShareMode [in]</dt>
	 * <dd>Flag that indicates whether other applications may form connections
	 * to this card.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SHARE_SHARED</td>
	 * <td>This application will share this card with other applications.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SHARE_EXCLUSIVE</td>
	 * <td>This application will not share this card with other applications.
	 * </td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>dwPreferredProtocols [in]</dt>
	 * <dd>Bitmask of acceptable protocols for this connection. Possible values
	 * may be combined with the OR operation. The value of this parameter should
	 * include the current protocol. Attempting to reconnect with a protocol
	 * other than the current protocol will result in an error.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T0</td>
	 * <td>T=0 is an acceptable protocol.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T1</td>
	 * <td>T=1 is an acceptable protocol.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>dwInitialization [in]</dt>
	 * <dd>Type of initialization that should be performed on the card.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_LEAVE_CARD</td>
	 * <td>Do not do anything special on reconnect.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_RESET_CARD</td>
	 * <td>Reset the card (Warm Reset).</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_UNPOWER_CARD</td>
	 * <td>Power down the card and reset it (Cold Reset).</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>pdwActiveProtocol [out, optional]</dt>
	 * <dd>Flag that indicates the established active protocol.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T0</td>
	 * <td>T=0 is the active protocol.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T1</td>
	 * <td>T=1 is the active protocol.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * </dl>
	 * 
	 * @return SCARDHANDLE
	 */
	int SCardReconnect(final SCARDHANDLE hCard, final long dwShareMode, final long dwPreferredProtocols,
			final long dwInitialization, final NativePcscDword pdwActiveProtocol);

	/**
	 * The SCardReleaseContext function closes an established resource manager
	 * context, freeing any resources allocated under that context, including
	 * SCARDHANDLE objects and memory allocated using the SCARD_AUTOALLOCATE
	 * length designator.
	 * 
	 * <pre>
	 * LONG SCardReleaseContext(
	 *   _In_ SCARDCONTEXT hContext
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hContext [in]</dt>
	 * <dd>Handle that identifies the resource manager context. The resource
	 * manager context is set by a previous call to
	 * {@link #SCardEstablishContext}.</dd>
	 * </dl>
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
	int SCardReleaseContext(final SCARDCONTEXT hContext);

	/**
	 * The SCardStatus function provides the current status of a smart card in a
	 * reader. You can call it any time after a successful call to SCardConnect
	 * and before a successful call to SCardDisconnect. It does not affect the
	 * state of the reader or reader driver.
	 * 
	 * <pre>
	 * LONG SCardStatus(
	 *   _In_        SCARDHANDLE hCard,
	 *   _Out_       LPTSTR      szReaderName,
	 *   _Inout_opt_ LPDWORD     pcchReaderLen,
	 *   _Out_opt_   LPDWORD     pdwState,
	 *   _Out_opt_   LPDWORD     pdwProtocol,
	 *   _Out_       LPBYTE      pbAtr,
	 *   _Inout_opt_ LPDWORD     pcbAtrLen
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hCard [in]</dt>
	 * <dd>Reference value returned from {@link #SCardConnect}.</dd>
	 * <dt>szReaderName [out]</dt>
	 * <dd>List of display names (multiple string) by which the currently
	 * connected reader is known.</dd>
	 * <dt>pcchReaderLen [in, out, optional]</dt>
	 * <dd>On input, supplies the length of the szReaderName buffer.<br />
	 * On output, receives the actual length (in characters) of the reader name
	 * list, including the trailing NULL character. If this buffer length is
	 * specified as SCARD_AUTOALLOCATE, then szReaderName is converted to a
	 * pointer to a byte pointer, and it receives the address of a block of
	 * memory that contains the multiple-string structure.</dd>
	 * <dt>pdwState [out, optional]</dt>
	 * <dd>Current state of the smart card in the reader. Upon success, it
	 * receives one of the following state indicators.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_ABSENT</td>
	 * <td>There is no card in the reader.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PRESENT</td>
	 * <td>There is a card in the reader, but it has not been moved into
	 * position for use.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SWALLOWED</td>
	 * <td>There is a card in the reader in position for use. The card is not
	 * powered.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_POWERED</td>
	 * <td>Power is being provided to the card, but the reader driver is unaware
	 * of the mode of the card.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_NEGOTIABLE</td>
	 * <td>The card has been reset and is awaiting PTS negotiation.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_SPECIFIC</td>
	 * <td>The card has been reset and specific communication protocols have
	 * been established.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>pdwProtocol [out, optional]</dt>
	 * <dd>Current protocol, if any. The returned value is meaningful only if
	 * the returned value of pdwState is SCARD_SPECIFICMODE.
	 * <table>
	 * <tr>
	 * <th>Value</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_RAW</td>
	 * <td>The Raw Transfer protocol is in use.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T0</td>
	 * <td>The ISO 7816/3 T=0 protocol is in use.</td>
	 * </tr>
	 * <tr>
	 * <td>SCARD_PROTOCOL_T1</td>
	 * <td>The ISO 7816/3 T=1 protocol is in use.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>pbAtr [out]</dt>
	 * <dd>Pointer to a 32-byte buffer that receives the ATR string from the
	 * currently inserted card, if available.</dd>
	 * <dt>pcbAtrLen [in, out, optional]</dt>
	 * <dd>On input, supplies the length of the pbAtr buffer. On output,
	 * receives the number of bytes in the ATR string (32 bytes maximum). If
	 * this buffer length is specified as SCARD_AUTOALLOCATE, then pbAtr is
	 * converted to a pointer to a byte pointer, and it receives the address of
	 * a block of memory that contains the multiple-string structure.</dd>
	 * </dl>
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
	int SCardStatus(final SCARDHANDLE hCard, final INativeObject szReaderName, final NativePcscDword pcchReaderLen,
			final NativePcscDword pdwState, final NativePcscDword pdwProtocol, final NativeBuffer pbAtr,
			final NativePcscDword pcbAtrLen);

	/**
	 * The SCardTransmit function sends a service request to the smart card and
	 * expects to receive data back from the card.
	 * 
	 * <pre>
	 * LONG SCardTransmit(
	 *   _In_        SCARDHANDLE         hCard,
	 *   _In_        LPCSCARD_IO_REQUEST pioSendPci,
	 *   _In_        LPCBYTE             pbSendBuffer,
	 *   _In_        DWORD               cbSendLength,
	 *   _Inout_opt_ LPSCARD_IO_REQUEST  pioRecvPci,
	 *   _Out_       LPBYTE              pbRecvBuffer,
	 *   _Inout_     LPDWORD             pcbRecvLength
	 * );
	 * </pre>
	 * 
	 * Parameters
	 * 
	 * <dl>
	 * <dt>hCard [in]</dt>
	 * <dd>A reference value returned from the {@link #SCardConnect} function.
	 * </dd>
	 * <dt>pioSendPci [in]</dt>
	 * <dd>A pointer to the protocol header structure for the instruction. This
	 * buffer is in the format of an SCARD_IO_REQUEST structure, followed by the
	 * specific protocol control information (PCI).<br />
	 * For the T=0, T=1, and Raw protocols, the PCI structure is constant. The
	 * smart card subsystem supplies a global T=0, T=1, or Raw PCI structure,
	 * which you can reference by using the symbols SCARD_PCI_T0, SCARD_PCI_T1,
	 * and SCARD_PCI_RAW respectively.</dd>
	 * <dt>pbSendBuffer [in]</dt>
	 * 
	 * <dd>A pointer to the actual data to be written to the card.<br />
	 * For T=0, the data parameters are placed into the address pointed to by
	 * pbSendBuffer according to the following structure:
	 * 
	 * <pre>
	 * struct {
	 *     BYTE
	 *         bCla,   // the instruction class
	 *         bIns,   // the instruction code 
	 *         bP1,    // parameter to the instruction
	 *         bP2,    // parameter to the instruction
	 *         bP3;    // size of I/O transfer
	 * } CmdBytes;
	 * </pre>
	 * 
	 * The data sent to the card should immediately follow the send buffer. In
	 * the special case where no data is sent to the card and no data is
	 * expected in return, bP3 is not sent.
	 * <table>
	 * <tr>
	 * <th>Member</th>
	 * <th>Meaning</th>
	 * </tr>
	 * <tr>
	 * <td>bCla</td>
	 * <td>The T=0 instruction class.</td>
	 * </tr>
	 * <tr>
	 * <td>bIns</td>
	 * <td>An instruction code in the T=0 instruction class.</td>
	 * </tr>
	 * <tr>
	 * <td>bP1, bP2</td>
	 * <td>Reference codes that complete the instruction code.</td>
	 * </tr>
	 * <tr>
	 * <td>bP3</td>
	 * <td>The number of data bytes to be transmitted during the command, per
	 * ISO 7816-4, Section 8.2.1.</td>
	 * </tr>
	 * </table>
	 * </dd>
	 * <dt>cbSendLength [in]</dt>
	 * <dd>The length, in bytes, of the pbSendBuffer parameter.<br />
	 * For T=0, in the special case where no data is sent to the card and no
	 * data expected in return, this length must reflect that the bP3 member is
	 * not being sent; the length should be sizeof(CmdBytes) - sizeof(BYTE).
	 * </dd>
	 * <dt>pioRecvPci [in, out, optional]</dt>
	 * <dd>Pointer to the protocol header structure for the instruction,
	 * followed by a buffer in which to receive any returned protocol control
	 * information (PCI) specific to the protocol in use. This parameter can be
	 * NULL if no PCI is returned.</dd>
	 * <dt>pbRecvBuffer [out]</dt>
	 * <dd>Pointer to any data returned from the card.<br />
	 * For T=0, the data is immediately followed by the SW1 and SW2 status
	 * bytes. If no data is returned from the card, then this buffer will only
	 * contain the SW1 and SW2 status bytes.</dd>
	 * <dt>pcbRecvLength [in, out]</dt>
	 * <dd>Supplies the length, in bytes, of the pbRecvBuffer parameter and
	 * receives the actual number of bytes received from the smart card. This
	 * value cannot be SCARD_AUTOALLOCATE because SCardTransmit does not support
	 * SCARD_AUTOALLOCATE.<br />
	 * For T=0, the receive buffer must be at least two bytes long to receive
	 * the SW1 and SW2 status bytes.</dd>
	 * </dl>
	 * 
	 * @throws PCSCException
	 */
	int SCardTransmit(final SCARDHANDLE hCard, final INativeHandle pioSendPci, final INativeObject pbSendBuffer,
			final long cbSendLength, final INativeObject pioRecvPci, final NativeBuffer pbRecvBuffer,
			final NativePcscDword pcbRecvLength);
}