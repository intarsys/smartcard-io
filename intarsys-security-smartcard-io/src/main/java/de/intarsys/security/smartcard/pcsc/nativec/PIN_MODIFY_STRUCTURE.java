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
 * typedef struct _PIN_MODIFY_STRUCTURE
 * {
 * BYTE bTimeOut; // timeout in seconds (00 means use default timeout)
 * BYTE bTimeOut2 // timeout in seconds after first key stroke
 * BYTE bmFormatString; // formatting options USB_CCID_PIN_FORMAT_xxx)
 * BYTE bmPINBlockString; // bits 7-4 bit size of PIN length in APDU, bits 3-0 PIN
 * // block size in bytes after justification and formatting
 * BYTE bmPINLengthFormat; // bits 7-5 RFU, bit 4 set if system units are bytes,
 * // clear if system units are bits
 * // bits 3-0 PIN length position in system units
 * // bits, bits 3-0 PIN length position in system units
 * BYTE bInsertionOffsetOld; // Insertion position offset in bytes for the current PIN
 * BYTE bInsertionOffsetNew; // Insertion position offset in bytes for the new PIN
 * USHORT wPINMaxExtraDigit; // XXYY, where XX is minimum PIN size in digits,
 * // YY is maximum
 * BYTE bConfirmPIN; // Flags governing need for confirmation of new PIN
 * BYTE bEntryValidationCondition; // Conditions under which PIN entry should be
 * // considered complete
 * BYTE bNumberMessage; // Number of messages to display for PIN verification
 * USHORT wLangId; // Language for messages
 * BYTE bMsgIndex1; // Index of 1st prompting message
 * BYTE bMsgIndex2; // Index of 2d prompting message
 * BYTE bMsgIndex3; // Index of 3d prompting message
 * BYTE bTeoPrologue[3]; // T=1 I-block prologue field to use (fill with 00)
 * ULONG ulDataLength // length of Data to be sent to the ICC
 * BYTE abData[1]; // Data to send to the ICC
 * } PIN_MODIFY_STRUCTURE , *PPIN_MODIFY_STRUCTURE ;
 * 
 * </pre>
 * 
 * This is a "dynamically" sized data structure as the APDU data is appended to
 * the bytes in memory.
 */
public class PIN_MODIFY_STRUCTURE extends NativeStaticStruct {

	/**
	 * The meta class implementation
	 */
	public static class MetaClass extends NativeStructType {
		protected MetaClass(Class<PIN_MODIFY_STRUCTURE> instanceClass) {
			super(instanceClass);
		}

		@Override
		public INativeObject createNative(INativeHandle handle) {
			return new PIN_MODIFY_STRUCTURE(handle);
		}
	}

	/** The meta class instance */
	public static final MetaClass META = new MetaClass(
			PIN_MODIFY_STRUCTURE.class);

	private static final StructMember abData;
	private static final StructMember bEntryValidationCondition;
	private static final StructMember bmFormatString;
	private static final StructMember bmPINBlockString;
	private static final StructMember bmPINLengthFormat;
	private static final StructMember bNumberMessage;
	private static final StructMember bTimeOut;
	private static final StructMember bTimeOut2;
	private static final StructMember ulDataLength;
	private static final StructMember wLangId;
	private static final StructMember wPINMaxExtraDigit;
	private static final StructMember bInsertionOffsetOld;
	private static final StructMember bInsertionOffsetNew;
	private static final StructMember bMsgIndex1;
	private static final StructMember bMsgIndex2;
	private static final StructMember bMsgIndex3;
	private static final StructMember bConfirmPIN;
	private static final StructMember bTeoPrologue;

	static {
		META.setPacking(1);
		bTimeOut = META.declare("bTimeOut", NativeByte.META); //$NON-NLS-1$
		bTimeOut2 = META.declare("bTimeOut2", NativeByte.META); //$NON-NLS-1$
		bmFormatString = META.declare("bmFormatString", NativeByte.META);//$NON-NLS-1$
		bmPINBlockString = META.declare("bmPINBlockString", NativeByte.META); //$NON-NLS-1$
		bmPINLengthFormat = META.declare("bmPINLengthFormat", NativeByte.META);//$NON-NLS-1$
		bInsertionOffsetOld = META.declare(
				"bInsertionOffsetOld", NativeByte.META);//$NON-NLS-1$
		bInsertionOffsetNew = META.declare(
				"bInsertionOffsetNew", NativeByte.META);//$NON-NLS-1$
		wPINMaxExtraDigit = META.declare("wPINMaxExtraDigit", NativeShort.META);//$NON-NLS-1$
		bConfirmPIN = META.declare("bConfirmPIN", NativeByte.META);//$NON-NLS-1$
		bEntryValidationCondition = META.declare("bEntryValidationCondition",//$NON-NLS-1$
				NativeByte.META);
		bNumberMessage = META.declare("bNumberMessage", NativeByte.META);//$NON-NLS-1$
		wLangId = META.declare("wLangId", NativeShort.META);//$NON-NLS-1$
		bMsgIndex1 = META.declare("bMsgIndex1", NativeByte.META);//$NON-NLS-1$
		bMsgIndex2 = META.declare("bMsgIndex2", NativeByte.META);//$NON-NLS-1$
		bMsgIndex3 = META.declare("bMsgIndex3", NativeByte.META);//$NON-NLS-1$
		bTeoPrologue = META.declare("bTeoPrologue", NativeByte.META.Array(3));//$NON-NLS-1$
		ulDataLength = META.declare("ulDataLength", NativeInt.META);//$NON-NLS-1$
		abData = META.declare("abData", NativeByte.META.Array(0));//$NON-NLS-1$
	}

	public static MetaClass getMETA() {
		return META;
	}

	private int abDataSize;

	protected PIN_MODIFY_STRUCTURE(INativeHandle handle) {
		super(handle);
	}

	public PIN_MODIFY_STRUCTURE(int pAbDataSize) {
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

	public byte getConfirmPIN() {
		NativeByte nByte = (NativeByte) bConfirmPIN.getNativeObject(this);
		return nByte.byteValue();
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

	public byte getInsertionOffsetNew() {
		NativeByte nByte = (NativeByte) bInsertionOffsetNew
				.getNativeObject(this);
		return nByte.byteValue();
	}

	public byte getInsertionOffsetOld() {
		NativeByte nByte = (NativeByte) bInsertionOffsetOld
				.getNativeObject(this);
		return nByte.byteValue();
	}

	public int getLangId() {
		NativeShort nShort = (NativeShort) wLangId.getNativeObject(this);
		return nShort.intValue();
	}

	public byte getMsgIndex1() {
		NativeByte nByte = (NativeByte) bMsgIndex1.getNativeObject(this);
		return nByte.byteValue();
	}

	public byte getMsgIndex2() {
		NativeByte nByte = (NativeByte) bMsgIndex2.getNativeObject(this);
		return nByte.byteValue();
	}

	public byte getMsgIndex3() {
		NativeByte nByte = (NativeByte) bMsgIndex3.getNativeObject(this);
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

	public void setConfirmPIN(int confirmPIN) {
		NativeByte nByte = (NativeByte) bConfirmPIN.getNativeObject(this);
		nByte.setValue((byte) confirmPIN);
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

	public void setInsertionOffsetNew(int insertionOffsetNew) {
		NativeByte nByte = (NativeByte) bInsertionOffsetNew
				.getNativeObject(this);
		nByte.setValue((byte) insertionOffsetNew);
	}

	public void setInsertionOffsetOld(int insertionOffsetOld) {
		NativeByte nByte = (NativeByte) bInsertionOffsetOld
				.getNativeObject(this);
		nByte.setValue((byte) insertionOffsetOld);
	}

	public void setLangId(int langId) {
		NativeShort nShort = (NativeShort) wLangId.getNativeObject(this);
		nShort.setValue(langId);
	}

	public void setMsgIndex1(int msgIndex) {
		NativeByte nByte = (NativeByte) bMsgIndex1.getNativeObject(this);
		nByte.setValue((byte) msgIndex);
	}

	public void setMsgIndex2(int msgIndex) {
		NativeByte nByte = (NativeByte) bMsgIndex2.getNativeObject(this);
		nByte.setValue((byte) msgIndex);
	}

	public void setMsgIndex3(int msgIndex) {
		NativeByte nByte = (NativeByte) bMsgIndex3.getNativeObject(this);
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
