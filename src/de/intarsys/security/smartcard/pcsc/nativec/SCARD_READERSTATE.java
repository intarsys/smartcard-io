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
package de.intarsys.security.smartcard.pcsc.nativec;

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.INativeObject;
import de.intarsys.nativec.type.INativeType;
import de.intarsys.nativec.type.NativeArray;
import de.intarsys.nativec.type.NativeByte;
import de.intarsys.nativec.type.NativeStaticStruct;
import de.intarsys.nativec.type.NativeString;
import de.intarsys.nativec.type.NativeStructType;
import de.intarsys.nativec.type.NativeVoid;
import de.intarsys.nativec.type.StructMember;

/**
 * <pre>
 * 
 *    typedef struct {
 *    LPCSTR      szReader;       // reader name
 *    LPVOID      pvUserData;     // user defined data
 *    DWORD       dwCurrentState; // current state of reader at time of call
 *    DWORD       dwEventState;   // state of reader after state change
 *    DWORD       cbAtr;          // Number of bytes in the returned ATR.
 *    BYTE        rgbAtr[36];     // ATR of inserted card, (extra alignment bytes)
 *    } SCARD_READERSTATE_A;
 * </pre>
 */
public class SCARD_READERSTATE extends NativeStaticStruct {

	/**
	 * The meta class implementation
	 */
	public static class MetaClass extends NativeStructType {
		protected MetaClass(Class<SCARD_READERSTATE> instanceClass) {
			super(instanceClass);
		}

		@Override
		public INativeObject createNative(INativeHandle handle) {
			return new SCARD_READERSTATE(handle);
		}
	}

	/** The meta class instance */
	public static final MetaClass META = new MetaClass(SCARD_READERSTATE.class);

	static final private StructMember cbAtr;

	static final private StructMember dwCurrentState;

	static final private StructMember dwEventState;

	static final private StructMember rgbAtr;

	static final private StructMember szReader;

	static {
		szReader = META.declare("szReader", NativeString.META.Ref()); //$NON-NLS-1$
		META.declare("pvUserData", NativeVoid.META.Ref());//$NON-NLS-1$
		dwCurrentState = META.declare("dwCurrentState", NativePcscDword.META);//$NON-NLS-1$
		dwEventState = META.declare("dwEventState", NativePcscDword.META);//$NON-NLS-1$
		cbAtr = META.declare("cbAtr", NativePcscDword.META);//$NON-NLS-1$
		rgbAtr = META.declare("rgbAtr", NativeByte.META.Array(36));//$NON-NLS-1$
	}

	public SCARD_READERSTATE() {
		super();
	}

	protected SCARD_READERSTATE(INativeHandle nativeHandle) {
		super(nativeHandle);
	}

	public byte[] getATR() {
		int size = getATRSize();
		NativeArray array = (NativeArray) rgbAtr.getNativeObject(this);
		return array.getByteArray(0, size);
	}

	public int getATRSize() {
		int size = (int) cbAtr.getCLong(this, 0);
		return size < 0 ? 0 : size;
	}

	public int getCurrentState() {
		return (int) dwCurrentState.getCLong(this, 0);
	}

	public int getEventState() {
		return (int) dwEventState.getCLong(this, 0);
	}

	@Override
	public INativeType getNativeType() {
		return META;
	}

	public String getReader() {
		return ((NativeString) szReader.getValue(this)).stringValue();
	}

	public void setATR(byte[] newAtr) {
		int length = newAtr.length;
		if (length > 36) {
			throw new IllegalArgumentException(
					"ATR cannot be longer than 36 bytes"); //$NON-NLS-1$
		}
		cbAtr.setInt(this, 0, newAtr.length);
		NativeArray array = (NativeArray) rgbAtr.getNativeObject(this);
		array.setByteArray(0, newAtr, 0, length);
	}

	public void setCurrentState(int state) {
		dwCurrentState.setValue(this, state);
	}

	public void setEventState(int state) {
		dwEventState.setValue(this, state);
	}

	public void setReader(NativeString name) {
		szReader.setValue(this, name);
	}

}
