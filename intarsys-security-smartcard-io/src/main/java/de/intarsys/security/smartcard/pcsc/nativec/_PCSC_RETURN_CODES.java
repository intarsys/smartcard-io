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

/**
 * Common PC/SC return and error codes.
 * 
 */
public interface _PCSC_RETURN_CODES {

	/* Time out happened from the semaphore api functions. */
	public static final int ERROR_SEM_TIMEOUT = 121;

	public static final int ERROR_INSUFFICIENT_BUFFER = 122;

	/* write on pipe with no reader */
	public static final int ERROR_BROKEN_PIPE = 109;

	public static final int ERROR_RPC_FAILED = 1727;

	public static final int ERROR_INVALID_FUNCTION = 1;

	public static final int ERROR_INVALID_HANDLE = 6;

	//
	// MessageId: SCARD_E_BAD_SEEK
	//
	// MessageText:
	//
	// There was an error trying to set the smart card file object pointer.
	//
	public static final int SCARD_E_BAD_SEEK = 0x80100029;

	// =============================
	// Facility SCARD Error Messages
	// =============================
	//

	//
	// MessageId: SCARD_E_CANCELLED
	//
	// MessageText:
	//
	// The action was cancelled by an SCardCancel request.
	//
	public static final int SCARD_E_CANCELLED = 0x80100002;

	//
	// MessageId: SCARD_E_CANT_DISPOSE
	//
	// MessageText:
	//
	// The system could not dispose of the media in the requested manner.
	//
	public static final int SCARD_E_CANT_DISPOSE = 0x8010000E;

	//
	// MessageId: SCARD_E_CARD_UNSUPPORTED
	//
	// MessageText:
	//
	// The smart card does not meet minimal requirements for support.
	//
	public static final int SCARD_E_CARD_UNSUPPORTED = 0x8010001C;

	//
	// MessageId: SCARD_E_CERTIFICATE_UNAVAILABLE
	//
	// MessageText:
	//
	// The requested certificate could not be obtained.
	//
	public static final int SCARD_E_CERTIFICATE_UNAVAILABLE = 0x8010002D;

	//
	// MessageId: SCARD_E_COMM_DATA_LOST
	//
	// MessageText:
	//
	// A communications error with the smart card has been detected. Retry the
	// operation.
	//
	public static final int SCARD_E_COMM_DATA_LOST = 0x8010002F;

	//
	// MessageId: SCARD_E_DIR_NOT_FOUND
	//
	// MessageText:
	//
	// The identified directory does not exist in the smart card.
	//
	public static final int SCARD_E_DIR_NOT_FOUND = 0x80100023;

	//
	// MessageId: SCARD_E_DUPLICATE_READER
	//
	// MessageText:
	//
	// The reader driver did not produce a unique reader name.
	//
	public static final int SCARD_E_DUPLICATE_READER = 0x8010001B;

	//
	// MessageId: SCARD_E_FILE_NOT_FOUND
	//
	// MessageText:
	//
	// The identified file does not exist in the smart card.
	//
	public static final int SCARD_E_FILE_NOT_FOUND = 0x80100024;

	//
	// MessageId: SCARD_E_ICC_CREATEORDER
	//
	// MessageText:
	//
	// The requested order of object creation is not supported.
	//
	public static final int SCARD_E_ICC_CREATEORDER = 0x80100021;

	//
	// MessageId: SCARD_E_ICC_INSTALLATION
	//
	// MessageText:
	//
	// No Primary Provider can be found for the smart card.
	//
	public static final int SCARD_E_ICC_INSTALLATION = 0x80100020;

	//
	// MessageId: SCARD_E_INSUFFICIENT_BUFFER
	//
	// MessageText:
	//
	// The data buffer to receive returned data is too small for the returned
	// data.
	//
	public static final int SCARD_E_INSUFFICIENT_BUFFER = 0x80100008;

	//
	// MessageId: SCARD_E_INVALID_ATR
	//
	// MessageText:
	//
	// An ATR obtained from the registry is not a valid ATR string.
	//
	public static final int SCARD_E_INVALID_ATR = 0x80100015;

	//
	// MessageId: SCARD_E_INVALID_CHV
	//
	// MessageText:
	//
	// The supplied PIN is incorrect.
	//
	public static final int SCARD_E_INVALID_CHV = 0x8010002A;

	//
	// MessageId: SCARD_E_INVALID_HANDLE
	//
	// MessageText:
	//
	// The supplied handle was invalid.
	//
	public static final int SCARD_E_INVALID_HANDLE = 0x80100003;

	//
	// MessageId: SCARD_E_INVALID_PARAMETER
	//
	// MessageText:
	//
	// One or more of the supplied parameters could not be properly interpreted.
	//
	public static final int SCARD_E_INVALID_PARAMETER = 0x80100004;

	//
	// MessageId: SCARD_E_INVALID_TARGET
	//
	// MessageText:
	//
	// Registry startup information is missing or invalid.
	//
	public static final int SCARD_E_INVALID_TARGET = 0x80100005;

	//
	// MessageId: SCARD_E_INVALID_VALUE
	//
	// MessageText:
	//
	// One or more of the supplied parameters values could not be properly
	// interpreted.
	//
	public static final int SCARD_E_INVALID_VALUE = 0x80100011;

	//
	// MessageId: SCARD_E_NO_ACCESS
	//
	// MessageText:
	//
	// Access is denied to this file.
	//
	public static final int SCARD_E_NO_ACCESS = 0x80100027;

	//
	// MessageId: SCARD_E_NO_DIR
	//
	// MessageText:
	//
	// The supplied path does not represent a smart card directory.
	//
	public static final int SCARD_E_NO_DIR = 0x80100025;

	//
	// MessageId: SCARD_E_NO_FILE
	//
	// MessageText:
	//
	// The supplied path does not represent a smart card file.
	//
	public static final int SCARD_E_NO_FILE = 0x80100026;

	//
	// MessageId: SCARD_E_NO_KEY_CONTAINER
	//
	// MessageText:
	//
	// The requested key container does not exist on the smart card.
	//
	public static final int SCARD_E_NO_KEY_CONTAINER = 0x80100030;

	//
	// MessageId: SCARD_E_NO_MEMORY
	//
	// MessageText:
	//
	// Not enough memory available to complete this command.
	//
	public static final int SCARD_E_NO_MEMORY = 0x80100006;

	//
	// MessageId: SCARD_E_NO_READERS_AVAILABLE
	//
	// MessageText:
	//
	// Cannot find a smart card reader.
	//
	public static final int SCARD_E_NO_READERS_AVAILABLE = 0x8010002E;

	//
	// MessageId: SCARD_E_NO_SERVICE
	//
	// MessageText:
	//
	// The Smart card resource manager is not running.
	//
	public static final int SCARD_E_NO_SERVICE = 0x8010001D;

	//
	// MessageId: SCARD_E_NO_SMARTCARD
	//
	// MessageText:
	//
	// The operation requires a Smart Card, but no Smart Card is currently in
	// the device.
	//
	public static final int SCARD_E_NO_SMARTCARD = 0x8010000C;

	//
	// MessageId: SCARD_E_NO_SUCH_CERTIFICATE
	//
	// MessageText:
	//
	// The requested certificate does not exist.
	//
	public static final int SCARD_E_NO_SUCH_CERTIFICATE = 0x8010002C;

	//
	// MessageId: SCARD_E_NOT_READY
	//
	// MessageText:
	//
	// The reader or smart card is not ready to accept commands.
	//
	public static final int SCARD_E_NOT_READY = 0x80100010;

	//
	// MessageId: SCARD_E_NOT_TRANSACTED
	//
	// MessageText:
	//
	// An attempt was made to end a non-existent transaction.
	//
	public static final int SCARD_E_NOT_TRANSACTED = 0x80100016;

	//
	// MessageId: SCARD_E_PCI_TOO_SMALL
	//
	// MessageText:
	//
	// The PCI Receive buffer was too small.
	//
	public static final int SCARD_E_PCI_TOO_SMALL = 0x80100019;

	//
	// MessageId: SCARD_E_PROTO_MISMATCH
	//
	// MessageText:
	//
	// The requested protocols are incompatible with the protocol currently in
	// use with the smart card.
	//
	public static final int SCARD_E_PROTO_MISMATCH = 0x8010000F;

	//
	// MessageId: SCARD_E_READER_UNAVAILABLE
	//
	// MessageText:
	//
	// The specified reader is not currently available for use.
	//
	public static final int SCARD_E_READER_UNAVAILABLE = 0x80100017;

	//
	// MessageId: SCARD_E_READER_UNSUPPORTED
	//
	// MessageText:
	//
	// The reader driver does not meet minimal requirements for support.
	//
	public static final int SCARD_E_READER_UNSUPPORTED = 0x8010001A;

	//
	// MessageId: SCARD_E_SERVER_TOO_BUSY
	//
	// MessageText:
	//
	// The Smart card resource manager is too busy to complete this operation.
	//
	public static final int SCARD_E_SERVER_TOO_BUSY = 0x80100031;

	//
	// MessageId: SCARD_E_SERVICE_STOPPED
	//
	// MessageText:
	//
	// The Smart card resource manager has shut down.
	//
	public static final int SCARD_E_SERVICE_STOPPED = 0x8010001E;

	//
	// MessageId: SCARD_E_SHARING_VIOLATION
	//
	// MessageText:
	//
	// The smart card cannot be accessed because of other connections
	// outstanding.
	//
	public static final int SCARD_E_SHARING_VIOLATION = 0x8010000B;

	//
	// MessageId: SCARD_E_SYSTEM_CANCELLED
	//
	// MessageText:
	//
	// The action was cancelled by the system, presumably to log off or shut
	// down.
	//
	public static final int SCARD_E_SYSTEM_CANCELLED = 0x80100012;

	//
	// MessageId: SCARD_E_TIMEOUT
	//
	// MessageText:
	//
	// The user-specified timeout value has expired.
	//
	public static final int SCARD_E_TIMEOUT = 0x8010000A;

	//
	// MessageId: SCARD_E_UNEXPECTED
	//
	// MessageText:
	//
	// An unexpected card error has occurred.
	//
	public static final int SCARD_E_UNEXPECTED = 0x8010001F;

	//
	// MessageId: SCARD_E_UNKNOWN_CARD
	//
	// MessageText:
	//
	// The specified smart card name is not recognized.
	//
	public static final int SCARD_E_UNKNOWN_CARD = 0x8010000D;

	//
	// MessageId: SCARD_E_UNKNOWN_READER
	//
	// MessageText:
	//
	// The specified reader name is not recognized.
	//
	public static final int SCARD_E_UNKNOWN_READER = 0x80100009;

	//
	// MessageId: SCARD_E_UNKNOWN_RES_MNG
	//
	// MessageText:
	//
	// An unrecognized error code was returned from a layered component.
	//
	public static final int SCARD_E_UNKNOWN_RES_MNG = 0x8010002B;

	//
	// MessageId: SCARD_E_UNSUPPORTED_FEATURE
	//
	// MessageText:
	//
	// This smart card does not support the requested feature.
	//
	public static final int SCARD_E_UNSUPPORTED_FEATURE = 0x80100022;

	//
	// MessageId: SCARD_E_WRITE_TOO_MANY
	//
	// MessageText:
	//
	// The smartcard does not have enough memory to store the information.
	//
	public static final int SCARD_E_WRITE_TOO_MANY = 0x80100028;

	//
	// MessageId: SCARD_F_COMM_ERROR
	//
	// MessageText:
	//
	// An internal communications error has been detected.
	//
	public static final int SCARD_F_COMM_ERROR = 0x80100013;

	//
	// MessageId: SCARD_F_INTERNAL_ERROR
	//
	// MessageText:
	//
	// An internal consistency check failed.
	//
	public static final int SCARD_F_INTERNAL_ERROR = 0x80100001;

	//
	// MessageId: SCARD_F_UNKNOWN_ERROR
	//
	// MessageText:
	//
	// An internal error has been detected, but the source is unknown.
	//
	public static final int SCARD_F_UNKNOWN_ERROR = 0x80100014;

	//
	// MessageId: SCARD_F_WAITED_TOO_LONG
	//
	// MessageText:
	//
	// An internal consistency timer has expired.
	//
	public static final int SCARD_F_WAITED_TOO_LONG = 0x80100007;

	//
	// MessageId: SCARD_P_SHUTDOWN
	//
	// MessageText:
	//
	// The operation has been aborted to allow the server application to exit.
	//
	public static final int SCARD_P_SHUTDOWN = 0x80100018;

	public static final int SCARD_S_SUCCESS = 0;

	//
	// MessageId: SCARD_W_CANCELLED_BY_USER
	//
	// MessageText:
	//
	// The action was cancelled by the user.
	//
	public static final int SCARD_W_CANCELLED_BY_USER = 0x8010006E;

	//
	// MessageId: SCARD_W_CARD_NOT_AUTHENTICATED
	//
	// MessageText:
	//
	// No PIN was presented to the smart card.
	//
	public static final int SCARD_W_CARD_NOT_AUTHENTICATED = 0x8010006F;

	//
	// MessageId: SCARD_W_CHV_BLOCKED
	//
	// MessageText:
	//
	// The card cannot be accessed because the maximum number of PIN entry
	// attempts has been reached.
	//
	public static final int SCARD_W_CHV_BLOCKED = 0x8010006C;

	//
	// MessageId: SCARD_W_EOF
	//
	// MessageText:
	//
	// The end of the smart card file has been reached.
	//
	public static final int SCARD_W_EOF = 0x8010006D;

	//
	// MessageId: SCARD_W_REMOVED_CARD
	//
	// MessageText:
	//
	// The smart card has been removed, so that further communication is not
	// possible.
	//
	public static final int SCARD_W_REMOVED_CARD = 0x80100069;

	//
	// MessageId: SCARD_W_RESET_CARD
	//
	// MessageText:
	//
	// The smart card has been reset, so any shared state information is
	// invalid.
	//
	public static final int SCARD_W_RESET_CARD = 0x80100068;

	//
	// MessageId: SCARD_W_SECURITY_VIOLATION
	//
	// MessageText:
	//
	// Access was denied because of a security violation.
	//
	public static final int SCARD_W_SECURITY_VIOLATION = 0x8010006A;

	//
	// MessageId: SCARD_W_UNPOWERED_CARD
	//
	// MessageText:
	//
	// Power has been removed from the smart card, so that further communication
	// is not possible.
	//
	public static final int SCARD_W_UNPOWERED_CARD = 0x80100067;

	//
	// MessageId: SCARD_W_UNRESPONSIVE_CARD
	//
	// MessageText:
	//
	// The smart card is not responding to a reset.
	//
	public static final int SCARD_W_UNRESPONSIVE_CARD = 0x80100066;

	//
	// These are warning codes.
	//
	//
	// MessageId: SCARD_W_UNSUPPORTED_CARD
	//
	// MessageText:
	//
	// The reader cannot communicate with the smart card, due to ATR
	// configuration conflicts.
	//
	public static final int SCARD_W_UNSUPPORTED_CARD = 0x80100065;

	//
	// MessageId: SCARD_W_WRONG_CHV
	//
	// MessageText:
	//
	// The card cannot be accessed because the wrong PIN was presented.
	//
	public static final int SCARD_W_WRONG_CHV = 0x8010006B;
}
