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

import de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES;
import de.intarsys.tools.message.IMessageBundle;
import de.intarsys.tools.reflect.ClassTools;

/**
 * An exceptional condition in the PC/SC handling
 * 
 */
public class PCSCException extends Exception implements _PCSC_RETURN_CODES {

	private static final long serialVersionUID = 1L;

	private static final IMessageBundle Msg = PACKAGE.Messages;

	public static void checkReturnCode(int rc) throws PCSCException {
		if (rc == SCARD_S_SUCCESS) {
			return;
		}
		/*
		 * these are "recoverable" states where the card rests in the terminal
		 */
		if (rc == //
		_PCSC_RETURN_CODES.SCARD_W_RESET_CARD //
				|| rc == _PCSC_RETURN_CODES.SCARD_W_UNPOWERED_CARD //
		) {
			throw new PCSCCardReset();
		}
		if (rc == //
		_PCSC_RETURN_CODES.SCARD_W_REMOVED_CARD //
				|| rc == _PCSC_RETURN_CODES.SCARD_W_UNRESPONSIVE_CARD //
				|| rc == _PCSC_RETURN_CODES.SCARD_W_UNSUPPORTED_CARD //
				|| rc == _PCSC_RETURN_CODES.SCARD_E_CARD_UNSUPPORTED //
				|| rc == _PCSC_RETURN_CODES.SCARD_E_NO_SMARTCARD) {
			throw new PCSCCardUnavailable(rc);
		}
		if (rc == _PCSC_RETURN_CODES.SCARD_E_SHARING_VIOLATION) {
			throw new PCSCSharingViolation(rc);
		}
		throw new PCSCException(rc);
	}

	public static void checkReturnCode(int rc, int size) throws PCSCException {
		if (rc == _PCSC_RETURN_CODES.ERROR_INSUFFICIENT_BUFFER
				|| rc == _PCSC_RETURN_CODES.SCARD_E_INSUFFICIENT_BUFFER) {
			throw new PCSCException(rc, ": at least " + size + " bytes required");
		}
		checkReturnCode(rc);
	}

	private int errorCode = 0;

	public PCSCException(int errorCode) {
		this.errorCode = errorCode;
	}

	public PCSCException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public PCSCException(String message) {
		super(message);
	}

	public PCSCException(String message, Throwable cause) {
		super(message, cause);
	}

	public int getErrorCode() {
		return errorCode;
	}

	@Override
	public String getMessage() {
		String tempMsg = super.getMessage();
		if (errorCode == 0) {
			return tempMsg;
		}
		if (tempMsg == null) {
			tempMsg = "";
		}

		String name = ClassTools.getConstantName(_PCSC_RETURN_CODES.class, errorCode);
		if (name != null) {
			String msg = Msg.getPattern("PCSCException.error." + name); //$NON-NLS-1$
			if (msg != null) {
				return Msg.format(msg, tempMsg);
			} else {
				return Msg.getString("PCSCException.error_default", name, //$NON-NLS-1$
						tempMsg);
			}
		} else {
			return Msg.getString("PCSCException.error_default", errorCode, tempMsg); //$NON-NLS-1$
		}
	}
}
