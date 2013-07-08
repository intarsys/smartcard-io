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

import de.intarsys.tools.system.SystemTools;

/**
 * A singleton access for the {@link IPCSCContextFactory} on this system.
 * 
 */
public class PCSCContextFactory {

	private static IPCSCContextFactory ACTIVE;

	private static boolean pcscLite = false;

	static {
		pcscLite = !SystemTools.isWindows();
	}

	static public IPCSCContextFactory get() {
		if (ACTIVE == null) {
			return NativePCSCContextFactory.get();
		}
		return ACTIVE;
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

	static public void set(IPCSCContextFactory active) {
		ACTIVE = active;
	}

	synchronized protected static void setPcscLite(boolean pPcscLite) {
		pcscLite = pPcscLite;
	}

}
