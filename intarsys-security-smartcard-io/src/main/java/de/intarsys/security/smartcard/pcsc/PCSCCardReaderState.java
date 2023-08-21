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

import java.util.HashMap;
import java.util.Map;

import de.intarsys.security.smartcard.pcsc.nativec.SCARD_READERSTATE;
import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.tools.hex.HexTools;
import de.intarsys.tools.string.IPrettyPrintable;
import de.intarsys.tools.string.PrettyPrinter;

/**
 * The PC/SC model level card reader state.
 * 
 */
public class PCSCCardReaderState implements IPrettyPrintable {

	public static final PCSCCardReaderState UNAWARE = new PCSCCardReaderState(null);

	private final SCARD_READERSTATE readerState;

	private static final Map<Integer, String> STATE_LABELS = new HashMap<Integer, String>();

	static {
		STATE_LABELS.put(_IPCSC.SCARD_STATE_ATRMATCH, "ATRMATCH");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_CHANGED, "CHANGED");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_EMPTY, "EMPTY");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_EXCLUSIVE, "EXCLUSIVE");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_IGNORE, "IGNORE");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_INUSE, "INUSE");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_MUTE, "MUTE");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_PRESENT, "PRESENT");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_UNAVAILABLE, "UNAVAILABLE");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_UNAWARE, "UNAWARE");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_UNKNOWN, "UNKNOWN");
		STATE_LABELS.put(_IPCSC.SCARD_STATE_UNPOWERED, "UNPOWERED");
	}

	protected PCSCCardReaderState(SCARD_READERSTATE readerState) {
		this.readerState = readerState;
	}

	public byte[] getATR() {
		if (readerState == null) {
			return null;
		}
		byte[] atr = readerState.getATR();
		if (atr.length < 5) {
			return null;
		}
		return atr;
	}

	public int getCurrentState() {
		if (readerState == null) {
			return _IPCSC.SCARD_STATE_UNAWARE;
		}
		return readerState.getCurrentState();
	}

	public int getEventState() {
		if (readerState == null) {
			return _IPCSC.SCARD_STATE_UNAWARE;
		}
		return readerState.getEventState();
	}

	public String getReader() {
		if (readerState == null) {
			return null;
		}
		return readerState.getReader();
	}

	public boolean isATRMatch() {
		return (getEventState() & _IPCSC.SCARD_STATE_ATRMATCH) != 0;
	}

	public boolean isChanged() {
		return (getEventState() & _IPCSC.SCARD_STATE_CHANGED) != 0;
	}

	public boolean isEmpty() {
		return (getEventState() & _IPCSC.SCARD_STATE_EMPTY) != 0;
	}

	public boolean isExclusive() {
		return (getEventState() & _IPCSC.SCARD_STATE_EXCLUSIVE) != 0;
	}

	public boolean isIgnore() {
		return (getEventState() & _IPCSC.SCARD_STATE_IGNORE) != 0;
	}

	public boolean isInUse() {
		return (getEventState() & _IPCSC.SCARD_STATE_INUSE) != 0;
	}

	public boolean isMute() {
		return (getEventState() & _IPCSC.SCARD_STATE_MUTE) != 0;
	}

	public boolean isPresent() {
		return (getEventState() & _IPCSC.SCARD_STATE_PRESENT) != 0;
	}

	protected boolean isSet(int state, Integer flag) {
		return (state & flag) != 0;
	}

	public boolean isUnavailable() {
		return (getEventState() & _IPCSC.SCARD_STATE_UNAVAILABLE) != 0;
	}

	public boolean isUnknown() {
		return (getEventState() & _IPCSC.SCARD_STATE_UNKNOWN) != 0;
	}

	private boolean isUnpowered() {
		return (getEventState() & _IPCSC.SCARD_STATE_UNPOWERED) != 0;
	}
	@Override
	public final String toString() {
		return new PrettyPrinter().toString(this);
	}

	@Override
	public void toString(PrettyPrinter printer) {
		if (readerState == null) {
			printer.appendString("<unaware>");
			return;
		}
		printer.appendMember("reader", readerState.getReader(), null);
		printer.appendMember("currentState", Integer.toBinaryString(readerState.getCurrentState()),
				toStringState(readerState.getCurrentState()));
		printer.appendMember("eventState", Integer.toBinaryString(readerState.getEventState()),
				toStringState(readerState.getEventState()));
		printer.appendMember("cAtr", readerState.getATRSize(), null);
		printer.appendMember("bAtr", HexTools.bytesToHexString(readerState.getATR()), null);
	}

	protected String toStringState(int state) {
		StringBuilder builder = new StringBuilder();
		for (Integer flag : STATE_LABELS.keySet()) {
			if (isSet(state, flag)) {
				if (builder.length() > 0) {
					builder.append(", "); //$NON-NLS-1$
				}
				builder.append(STATE_LABELS.get(flag));
			}
		}
		return builder.toString();
	}
}
