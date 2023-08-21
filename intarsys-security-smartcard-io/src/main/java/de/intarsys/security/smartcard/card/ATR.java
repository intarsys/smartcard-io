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
package de.intarsys.security.smartcard.card;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.intarsys.tools.collection.ByteArrayTools;
import de.intarsys.tools.hex.HexTools;
import de.intarsys.tools.string.StringTools;

/**
 * A wrapper for the "Answer to reset" byte stream. The ATR is signaled with the
 * "CardInserted" event from the PCSC layer.
 * 
 */
public class ATR {

	class ATRComponent {
		public String label = "";
		public String description = "";
		public int offset;
		public int length;

		public ATRComponent(String label, int offset, int length) {
			super();
			this.label = label;
			this.offset = offset;
			this.length = length;
		}

		public byte[] getValueArray() {
			return ByteArrayTools.copy(atr, offset, length);
		}

		public int getValueInt() {
			return Byte.toUnsignedInt(atr[offset]);
		}
	}

	public static final byte[] ATR_INTERFACE_CONTACTLESS = HexTools.hexStringToBytes("8001");

	public static ATR create(byte[] atr) {
		if (atr == null) {
			return null;
		}
		return new ATR(atr);
	}

	private final byte[] atr;

	private List<ATRComponent> components = new ArrayList<>();

	private int historicalCharactersOffset;

	private int tck = -1;

	public ATR(byte[] fullAtr) {
		assert (fullAtr != null);
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

	public int getCategoryIndicator() {
		if (getHistoricalBytesSize() == 0) {
			return 0xff;
		}
		return atr[historicalCharactersOffset];
	}

	public int getDataCodingByte() {
		// NotYetImplemented
		return 0;
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

	public int getLcs() {
		/*
		 * if (getCategoryIndicator() == 0x00) {
		 * // status indicator present at last three hsitorical bytes,
		 * // unencoded
		 * }
		 * if (getCategoryIndicator() == 0x80) {
		 * // status indicator present as last compact tlv object
		 * }
		 */
		return 0;
	}

	public int getSupportedLogicalChannels() {
		// NotYetImplemented
		return 0;
	}

	public int getSupportedSelectionMethods() {
		// NotYetImplemented
		return 0;
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
		return Arrays.hashCode(atr);
	}

	public boolean isContactless() {
		return Arrays.equals(getInterfaceBytes(), ATR.ATR_INTERFACE_CONTACTLESS);
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

	protected void parse() {
		if (components.size() > 0) {
			return;
		}
		ATRComponent component;
		int offset = 0;
		int length;
		int value;
		int historicalCount;
		boolean taiPresent;
		boolean tbiPresent;
		boolean tciPresent;
		boolean tdiPresent;
		int interfaceIndex = 0;
		//
		length = 1;
		component = new ATRComponent("TS", offset, length);
		offset += length;
		components.add(component);
		value = component.getValueInt();
		if (value == 0x3b) {
			component.description += "Direct convention";
		} else if (value == 0x3f) {
			component.description += "Inverse convention";
		} else {
			component.description += "Unknown convention";
		}
		//
		length = 1;
		component = new ATRComponent("T0", offset, length);
		offset += length;
		components.add(component);
		value = component.getValueInt();
		historicalCount = (value & 0x0f);
		component.description = "Format Byte<br>";
		interfaceIndex++;
		taiPresent = (value & 0x10) == 0x10;
		tbiPresent = (value & 0x20) == 0x20;
		tciPresent = (value & 0x40) == 0x40;
		tdiPresent = (value & 0x80) == 0x80;
		if (taiPresent) {
			component.description += "TA" + interfaceIndex + " available<br>";
		}
		if (tbiPresent) {
			component.description += "TB" + interfaceIndex + " available<br>";
		}
		if (tciPresent) {
			component.description += "TC" + interfaceIndex + " available<br>";
		}
		if (tdiPresent) {
			component.description += "TD" + interfaceIndex + " available<br>";
		}
		component.description += "" + historicalCount + " historical bytes";
		//
		while (taiPresent || tbiPresent || tciPresent || tdiPresent) {
			if (taiPresent) {
				length = 1;
				component = new ATRComponent("TA" + interfaceIndex, offset, length);
				offset += length;
				components.add(component);
				value = component.getValueInt();
				taiPresent = false;
			}
			if (tbiPresent) {
				length = 1;
				component = new ATRComponent("TB" + interfaceIndex, offset, length);
				offset += length;
				components.add(component);
				value = component.getValueInt();
				tbiPresent = false;
			}
			if (tciPresent) {
				length = 1;
				component = new ATRComponent("TC" + interfaceIndex, offset, length);
				offset += length;
				components.add(component);
				value = component.getValueInt();
				tciPresent = false;
			}
			if (tdiPresent) {
				length = 1;
				component = new ATRComponent("TD" + interfaceIndex, offset, length);
				offset += length;
				components.add(component);
				value = component.getValueInt();
				interfaceIndex++;
				taiPresent = (value & 0x10) == 0x10;
				tbiPresent = (value & 0x20) == 0x20;
				tciPresent = (value & 0x40) == 0x40;
				tdiPresent = (value & 0x80) == 0x80;
				if (taiPresent) {
					component.description += "TA" + interfaceIndex + " available<br>";
				}
				if (tbiPresent) {
					component.description += "TB" + interfaceIndex + " available<br>";
				}
				if (tciPresent) {
					component.description += "TC" + interfaceIndex + " available<br>";
				}
				if (tdiPresent) {
					component.description += "TD" + interfaceIndex + " available<br>";
				}
			}
		}
		if (historicalCount > 0) {
			length = historicalCount;
			component = new ATRComponent("Historical Bytes", offset, length);
			offset += length;
			components.add(component);
		}
		if (offset < atr.length) {
			length = 1;
			component = new ATRComponent("TCK", offset, length);
			offset += length;
			components.add(component);
		}
	}

	public String print() {
		return HexTools.bytesToHexString(atr);
	}

	public boolean supportsBerTlvFF() {
		// NotYetImplemented
		return true;
	}

	public boolean supportsCommandChaining() {
		// NotYetImplemented
		return true;
	}

	public boolean supportsExtendedLength() {
		// NotYetImplemented
		return true;
	}

	public boolean supportsShortEfIdentifier() {
		// NotYetImplemented
		return true;
	}

	@Override
	public String toString() {
		return HexTools.bytesToHexString(atr);
	}

	public String toStringDetail() {
		parse();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<table border=\"1\" >");
		sb.append("		<tr>");
		sb.append("			<td>");
		sb.append("				ATR");
		sb.append("			</td>");
		sb.append("			<td>");
		sb.append(HexTools.bytesToHexString(atr));
		sb.append("			</td>");
		sb.append("		</tr>");
		for (ATRComponent component : components) {
			sb.append("		<tr>");
			sb.append("			<td>");
			sb.append(component.label);
			sb.append("			</td>");
			sb.append("			<td>");
			sb.append(HexTools.bytesToHexString(component.getValueArray()));
			sb.append("			</td>");
			sb.append("		</tr>");
			if (!StringTools.isEmpty(component.description)) {
				sb.append("		<tr>");
				sb.append("			<td>");
				sb.append("			</td>");
				sb.append("			<td>");
				sb.append(component.description);
				sb.append("			</td>");
				sb.append("		</tr>");
			}
		}
		sb.append("</table>");
		sb.append("</html>");
		return sb.toString();
	}
}
