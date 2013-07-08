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

import de.intarsys.nativec.api.CLong;
import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.INativeObject;
import de.intarsys.nativec.type.NativeNumberType;
import de.intarsys.nativec.type.NativeObject;
import de.intarsys.tools.system.SystemTools;

/**
 * For some reason the DWord in PC/SC Lite header files is declared as 8 byte in
 * 64 bit Linux.
 * 
 * On all other platforms in this universe a DWord is 4 byte.
 * 
 */
public class NativePcscDwordType extends NativeNumberType {

	/**
	 * Utility method: return the given number as another number object with
	 * compatible byte size
	 */
	public static Object coerce(Number value) {
		if (SystemTools.isLinux()) {
			return new CLong(value.longValue());
		}
		return value.intValue();
	}

	/**
	 * Utility method: return the java class whose instances have compatible
	 * byte size
	 */
	public static Class<? extends Number> primitiveClass() {
		if (SystemTools.isLinux()) {
			return CLong.class;
		}
		return Integer.class;
	}

	@Override
	public INativeObject createNative(INativeHandle handle) {
		return new NativePcscDword(handle);
	}

	@Override
	public INativeObject createNative(Object value) {
		return new NativePcscDword(((Number) value).longValue());
	}

	@Override
	public int getByteCount() {
		if (SystemTools.isLinux()) {
			return NativeObject.SIZE_LONG;
		}
		return NativeObject.SIZE_INT;
	}

}
