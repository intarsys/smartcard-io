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
package de.intarsys.security.smartcard.card;

import java.util.Arrays;

import de.intarsys.tools.hex.HexTools;

/**
 * A wrapper for the "Answer to reset" byte stream. The ATR is signaled with the
 * "CardInserted" event from the PCSC layer.
 * 
 */
public class ATR {

	public static ATR create(byte[] atr) {
		if (atr == null) {
			return null;
		}
		return new ATR(atr);
	}

	final private byte[] atr;

	private int historicalCharactersOffset;

	private int tck = -1;

	public static final byte[] ATR_INTERFACE_CONTACTLESS = HexTools
			.hexStringToBytes("8001");

	public ATR(byte[] fullAtr) {
		assert (fullAtr != null);
		assert (fullAtr.length > 5);
		this.atr = fullAtr;

		historicalCharactersOffset = 1;
		boolean tdNextPresent = true;
		while (tdNextPresent) {
			byte tdi = atr[historicalCharactersOffset];
			tdNextPresent = (tdi & 0x80) != 0;
			int chunkLength = 0;
			if ((tdi & 0x80) != 0) {
				chunkLength++;
			}
			if ((tdi & 0x40) != 0) {
				chunkLength++;
			}
			if ((tdi & 0x20) != 0) {
				chunkLength++;
			}
			if ((tdi & 0x10) != 0) {
				chunkLength++;
			}
			historicalCharactersOffset += chunkLength;
		}
		historicalCharactersOffset++;
		if (getHistoricalBytesSize() + getHistoricalBytesOffset() < atr.length) {
			tck = atr[getHistoricalBytesSize() + getHistoricalBytesOffset()];
		}
	}

	public boolean equals(byte[] other) {
		// workaround for os x bug
		byte[] compare1;
		byte[] compare2;

		if (atr.length == other.length) {
			compare1 = atr;
			compare2 = other;
		} else {
			byte[] workaround;
			if (atr.length > other.length) {
				compare1 = other;
				workaround = atr;
			} else {
				compare1 = atr;
				workaround = other;
			}
			compare2 = new byte[compare1.length];
			System.arraycopy(workaround, 0, compare2, 0, compare2.length);
		}
		return Arrays.equals(compare1, compare2);
	}

	@Override
	public boolean equals(Object other) {
		boolean result = super.equals(other);
		if (!result) {
			if (other instanceof byte[]) {
				return equals((byte[]) other);
			} else if (other instanceof ATR) {
				return equals(((ATR) other).getBytes());
			}
		}
		return result;
	}

	public byte[] getBytes() {
		return atr.clone();
	}

	public byte[] getHistoricalBytes() {
		byte[] hist = new byte[getHistoricalBytesSize()];
		System.arraycopy(atr, historicalCharactersOffset, hist, 0, hist.length);
		return hist;
	}

	public int getHistoricalBytesOffset() {
		return historicalCharactersOffset;
	}

	public int getHistoricalBytesSize() {
		return atr[1] & 0x0f;
	}

	public byte[] getInterfaceBytes() {
		int count = getHistoricalBytesOffset() - 2;
		byte[] result = new byte[count];
		System.arraycopy(atr, 2, result, 0, result.length);
		return result;
	}

	public int getInterfaceBytesOffset() {
		return 2;
	}

	public int getInterfaceBytesSize() {
		return getHistoricalBytesOffset() - 2;
	}

	public int getT0() {
		return atr[1];
	}

	public int getTck() {
		return tck;
	}

	public int getTs() {
		return atr[0];
	}

	@Override
	public int hashCode() {
		return atr.hashCode();
	}

	public boolean isContactless() {
		return getInterfaceBytes().equals(ATR.ATR_INTERFACE_CONTACTLESS);
	}

	public boolean isDirectConvention() {
		return (atr[0] & 0x3b) == 0x3b;
	}

	public boolean isInverseConvention() {
		return (atr[0] & 0x3f) == 0x3f;
	}

	public boolean isTa1Available() {
		return (atr[1] & 0x10) == 0x10;
	}

	public boolean isTb1Available() {
		return (atr[1] & 0x20) == 0x20;
	}

	public boolean isTc1Available() {
		return (atr[1] & 0x40) == 0x40;
	}

	public boolean isTd1Available() {
		return (atr[1] & 0x80) == 0x80;
	}

	public void setHistoricalCharactersOffset(int historicalCharactersOffset) {
		this.historicalCharactersOffset = historicalCharactersOffset;
	}

	@Override
	public String toString() {
		return HexTools.bytesToHexString(atr);
	}
}
