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

import static de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES.SCARD_S_SUCCESS;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES;
import de.intarsys.tools.string.StringTools;
import de.intarsys.tools.system.SystemTools;

public class PCSCTools {

	private static boolean pcscLite = false;

	private static final Logger Log = PACKAGE.Log;

	static {
		pcscLite = !SystemTools.isWindows();
	}

	public static void checkReturnCode(int rc) throws PCSCException {
		if (rc == SCARD_S_SUCCESS) {
			return;
		}
		if (rc == _PCSC_RETURN_CODES.SCARD_W_RESET_CARD) {
			if (Log.isLoggable(Level.FINEST)) {
				Log.info("card was reset"); //$NON-NLS-1$ 
			}
			throw new PCSCReset();
		}
		throw new PCSCException(rc);
	}

	synchronized public static boolean isPcscLite() {
		return pcscLite;
	}

	synchronized static public int mapControlCode(int code) {
		if (pcscLite) {
			return 0x42000000 | code;
		}
		return 0x310000 | (code << 2);
	}

	synchronized protected static void setPcscLite(boolean pPcscLite) {
		pcscLite = pPcscLite;
	}

	static public String toString(byte[] buffer) {
		if (buffer == null || buffer.length == 0) {
			return StringTools.EMPTY;
		}
		int length = buffer.length;
		while (length > 0 && buffer[length - 1] == 0) {
			// remove terminating 0 byte
			length--;
		}
		return new String(buffer, 0, length);
	}

	private PCSCTools() {
		// tool class
	}

}
