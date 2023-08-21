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
import de.intarsys.nativec.type.INativeType;
import de.intarsys.nativec.type.NativeArray;
import de.intarsys.nativec.type.NativeByte;
import de.intarsys.nativec.type.NativeInt;
import de.intarsys.nativec.type.NativeShort;
import de.intarsys.nativec.type.NativeStaticStruct;
import de.intarsys.nativec.type.NativeStructType;
import de.intarsys.nativec.type.StructMember;

/**
 * <pre>
 * typedef struct _PIN_VERIFY_STRUCTURE
 *  {
 * 
 *  BYTE bTimeOut; // timeout in seconds (00 means use default timeout)
 *  BYTE bTimeOut2 // timeout in seconds after first key stroke
 *  BYTE bmFormatString; // formatting options
 *  // Bit   7: Unit: 0=Bits, 1=Bytes
 *  // Bit 6-3: PIN position in APDU after Lc
 *  // Bit   2: PIN-Justification: 0=Left 1=Right
 *  // Bit 1-0: PIN-Format: 00=binary 01=BCD 10=Ascii
 *  BYTE bmPINBlockString;  // PIN block definition
 *  // bits 7-4 bit size of PIN length in APDU
 *  // bits 3-0 PIN block size in bytes after justification and formatting
 *  BYTE bmPINLengthFormat; // PIN length definition 
 *  // bits 7-5 RFU
 *  // bit 4 set if system units are bytes, clear if system units are bits
 *  // bits 3-0 PIN length position in system units
 *  USHORT wPINMaxExtraDigit; // PIN length constraints XXYY 
 *  // XX is minimum PIN size in digits,
 *  // YY is maximum PIN size in digits,
 *  BYTE bEntryValidationCondition; // Conditions under which PIN entry should be
 *  // considered complete
 *  BYTE bNumberMessage; // Number of messages to display for PIN verification
 *  USHORT wLangId; // Language for messages
 *  BYTE bMsgIndex; // Message index (should be 00)
 *  BYTE bTeoPrologue[3]; // T=1 I-block prologue field to use (fill with 00)
 *  ULONG ulDataLength // length of Data to be sent to the ICC
 *  BYTE abData[1]; // Data to send to the ICC
 * 
 *  } PIN_VERIFY_STRUCTURE, *PPIN_VERIFY_STRUCTURE;
 * 
 * </pre>
 * 
 * This is a "dynamically" sized data structure as the APDU data is appended to
 * the bytes in memory.
 */
public class PIN_VERIFY_STRUCTURE extends NativeStaticStruct {

	/**
	 * The meta class implementation
	 */
	public static class MetaClass extends NativeStructType {
		protected MetaClass(Class<PIN_VERIFY_STRUCTURE> instanceClass) {
			super(instanceClass);
		}

		@Override
		public INativeObject createNative(INativeHandle handle) {
			return new PIN_VERIFY_STRUCTURE(handle);
		}
	}

	/** The meta class instance */
	public static final MetaClass META = new MetaClass(
			PIN_VERIFY_STRUCTURE.class);

	private static final StructMember abData;
	private static final StructMember bEntryValidationCondition;
	private static final StructMember bmFormatString;
	private static final StructMember bmPINBlockString;
	private static final StructMember bmPINLengthFormat;
	private static final StructMember bMsgIndex;
	private static final StructMember bNumberMessage;
	private static final StructMember bTimeOut;
	private static final StructMember bTimeOut2;
	private static final StructMember ulDataLength;
	private static final StructMember wLangId;
	private static final StructMember wPINMaxExtraDigit;
	private static final StructMember bTeoPrologue;

	static {
		META.setPacking(1);
		bTimeOut = META.declare("bTimeOut", NativeByte.META); //$NON-NLS-1$
		bTimeOut2 = META.declare("bTimeOut2", NativeByte.META); //$NON-NLS-1$
		bmFormatString = META.declare("bmFormatString", NativeByte.META);//$NON-NLS-1$
		bmPINBlockString = META.declare("bmPINBlockString", NativeByte.META); //$NON-NLS-1$
		bmPINLengthFormat = META.declare("bmPINLengthFormat", NativeByte.META);//$NON-NLS-1$
		wPINMaxExtraDigit = META.declare("wPINMaxExtraDigit", NativeShort.META);//$NON-NLS-1$
		bEntryValidationCondition = META.declare("bEntryValidationCondition",//$NON-NLS-1$
				NativeByte.META);
		bNumberMessage = META.declare("bNumberMessage", NativeByte.META);//$NON-NLS-1$
		wLangId = META.declare("wLangId", NativeShort.META);//$NON-NLS-1$
		bMsgIndex = META.declare("bMsgIndex", NativeByte.META);//$NON-NLS-1$
		bTeoPrologue = META.declare("bTeoPrologue", NativeByte.META.Array(3));//$NON-NLS-1$
		ulDataLength = META.declare("ulDataLength", NativeInt.META);//$NON-NLS-1$
		abData = META.declare("abData", NativeByte.META.Array(0));//$NON-NLS-1$
	}

	public static MetaClass getMETA() {
		return META;
	}

	private int abDataSize;

	protected PIN_VERIFY_STRUCTURE(INativeHandle handle) {
		super(handle);
	}

	public PIN_VERIFY_STRUCTURE(int pAbDataSize) {
		abDataSize = pAbDataSize;
		allocate();
		bTeoPrologue.setByteArray(this, 0, new byte[] { 0, 0, 0 }, 0, 3);
	}

	public byte[] getApdu() {
		NativeArray nArray = (NativeArray) abData.getNativeObject(this);
		return nArray.getByteArray(0, getDataLength());
	}

	@Override
	public int getByteCount() {
		return super.getByteCount() + abDataSize;
	}

	public int getDataLength() {
		NativeInt nInt = (NativeInt) ulDataLength.getNativeObject(this);
		return nInt.intValue();
	}

	public byte getEntryValidationCondition() {
		NativeByte nByte = (NativeByte) bEntryValidationCondition
				.getNativeObject(this);
		return nByte.byteValue();
	}

	public byte getFormatString() {
		NativeByte nByte = (NativeByte) bmFormatString.getNativeObject(this);
		return nByte.byteValue();
	}

	public int getLangId() {
		NativeShort nShort = (NativeShort) wLangId.getNativeObject(this);
		return nShort.intValue();
	}

	public byte getMsgIndex() {
		NativeByte nByte = (NativeByte) bMsgIndex.getNativeObject(this);
		return nByte.byteValue();
	}

	@Override
	public INativeType getNativeType() {
		return META;
	}

	public byte getNumberMessage() {
		NativeByte nByte = (NativeByte) bNumberMessage.getNativeObject(this);
		return nByte.byteValue();
	}

	public byte getPINBlockString() {
		NativeByte nByte = (NativeByte) bmPINBlockString.getNativeObject(this);
		return nByte.byteValue();
	}

	public byte getPINLengthFormat() {
		NativeByte nByte = (NativeByte) bmPINLengthFormat.getNativeObject(this);
		return nByte.byteValue();
	}

	public int getPINMaxExtraDigit() {
		NativeShort nShort = (NativeShort) wPINMaxExtraDigit
				.getNativeObject(this);
		return nShort.intValue();
	}

	public byte getTimeOut() {
		NativeByte nByte = (NativeByte) bTimeOut.getNativeObject(this);
		return nByte.byteValue();
	}

	public byte getTimeOut2() {
		NativeByte nByte = (NativeByte) bTimeOut2.getNativeObject(this);
		return nByte.byteValue();
	}

	public void setApdu(byte[] apdu, int apduOffset, int apduLength) {
		NativeArray nArray = (NativeArray) abData.getNativeObject(this);
		nArray.setByteArray(0, apdu, apduOffset, apduLength);
		setDataLength(apduLength);
	}

	protected void setDataLength(int apduLength) {
		NativeInt nInt = (NativeInt) ulDataLength.getNativeObject(this);
		nInt.setValue(apduLength);
	}

	public void setEntryValidationCondition(int condition) {
		NativeByte nByte = (NativeByte) bEntryValidationCondition
				.getNativeObject(this);
		nByte.setValue((byte) condition);
	}

	public void setFormatString(int formatString) {
		NativeByte nByte = (NativeByte) bmFormatString.getNativeObject(this);
		nByte.setValue((byte) formatString);
	}

	public void setLangId(int langId) {
		NativeShort nShort = (NativeShort) wLangId.getNativeObject(this);
		nShort.setValue(langId);
	}

	public void setMsgIndex(int msgIndex) {
		NativeByte nByte = (NativeByte) bMsgIndex.getNativeObject(this);
		nByte.setValue((byte) msgIndex);
	}

	public void setNumberMessage(int numberMessage) {
		NativeByte nByte = (NativeByte) bNumberMessage.getNativeObject(this);
		nByte.setValue((byte) numberMessage);
	}

	public void setPINBlockString(int pinBlockString) {
		NativeByte nByte = (NativeByte) bmPINBlockString.getNativeObject(this);
		nByte.setValue((byte) pinBlockString);
	}

	public void setPINLengthFormat(int pinLengthFormat) {
		NativeByte nByte = (NativeByte) bmPINLengthFormat.getNativeObject(this);
		nByte.setValue((byte) pinLengthFormat);
	}

	public void setPINMaxExtraDigit(int pinMaxExtraDigit) {
		NativeShort nShort = (NativeShort) wPINMaxExtraDigit
				.getNativeObject(this);
		nShort.setValue(pinMaxExtraDigit);
	}

	public void setPINMaxExtraDigit(int min, int max) {
		setPINMaxExtraDigit((min << 8) | max);
	}

	public void setTimeOut(int timeout) {
		NativeByte nByte = (NativeByte) bTimeOut.getNativeObject(this);
		nByte.setValue(timeout);
	}

	public void setTimeOut2(int timeout) {
		NativeByte nByte = (NativeByte) bTimeOut2.getNativeObject(this);
		nByte.setValue(timeout);
	}
}
