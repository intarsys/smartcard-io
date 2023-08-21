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

import java.util.function.BiFunction;

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.NativeBuffer;
import de.intarsys.nativec.type.NativeBufferType;
import de.intarsys.nativec.type.NativeObject;
import de.intarsys.nativec.type.NativeVoid;
import de.intarsys.security.smartcard.pcsc.nativec.NativePcscDword;
import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;
import de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES;
import de.intarsys.tools.string.StringTools;

/**
 * Some tools for handling PC/SC.
 * 
 */
public final class PCSCTools {

	/**
	 * Variant of IBufferHelper that will when called call the provided function
	 * in turn with the needed input to have the native code autoallocate memory
	 * for the buffer. We also avoid clogging the finalize queue with native
	 * objects because allocate and free is done in native code.
	 */
	private static class Autoallocate implements IBufferHelper {

		private NativePcscDword bufferSize = new NativePcscDword();
		private NativeBuffer buffer = new NativeBuffer(NativeObject.SIZE_PTR);

		@Override
		public synchronized byte[] call(CommonPCSCContext context,
				BiFunction<NativeBuffer, NativePcscDword, Integer> function) throws PCSCException {
			bufferSize.setValue(_IPCSC.SCARD_AUTOALLOCATE);
			int rc = function.apply(buffer, bufferSize);
			PCSCException.checkReturnCode(rc);
			INativeHandle handle = buffer.getNativeHandle(0);
			try {
				return ((NativeBuffer) NativeBufferType.create(bufferSize.intValue()).createNative(handle)).getBytes();
			} finally {
				context.getPcsc().SCardFreeMemory(context.getHContext(), NativeVoid.META.createNative(handle));
			}
		}
	}

	/**
	 * Helper interface for PCSC functions that have a (single) byte buffer
	 * output parameter.
	 */
	static interface IBufferHelper {

		/**
		 * Allocate the necessary bufferSize and buffer input variables, use
		 * them as input for the given function, and return the buffer's output
		 * bytes. What is allocated and how function is called might vary with
		 * what the underlying platform supports.
		 */
		byte[] call(CommonPCSCContext context, BiFunction<NativeBuffer, NativePcscDword, Integer> function)
				throws PCSCException;
	}

	/**
	 * Variant of IBufferHelper that will when called call the provided function
	 * in turn with a buffer allocated with a fixed size of memory and try again
	 * with a larger buffer in case of an "insufficient buffer" error code. The
	 * buffer object is reused and its size will always increase if needed but
	 * never made smaller again. We assume that we won't encounter any insane
	 * buffer requirements.
	 */
	private static class Preallocate implements IBufferHelper {

		NativePcscDword bufferSize = new NativePcscDword(0);
		// arbitrary size
		NativeBuffer buffer = new NativeBuffer(256);

		@Override
		public synchronized byte[] call(CommonPCSCContext context,
				BiFunction<NativeBuffer, NativePcscDword, Integer> function) throws PCSCException {
			while (true) {
				bufferSize.setValue(buffer.getSize());
				int rc = function.apply(buffer, bufferSize);
				if (rc == _PCSC_RETURN_CODES.SCARD_E_INSUFFICIENT_BUFFER) {
					buffer = new NativeBuffer(bufferSize.intValue());
					continue;
				}
				PCSCException.checkReturnCode(rc);
				return buffer.getByteArray(0, bufferSize.intValue());
			}
		}
	}

	public static IBufferHelper BufferHelper = _IPCSC.SupportsAutoallocate ? new Autoallocate() : new Preallocate();
	public static int DirectConnectProtocol = _IPCSC.SupportsProtocolUndefined ? _IPCSC.SCARD_PROTOCOL_UNDEFINED
			: _IPCSC.SCARD_PROTOCOL_Tx;

	/**
	 * Convert the resuilt of an {@link IPCSCConnection#getAttrib(int)} to a
	 * String. You must be prepared to get a zero terminated byte array.
	 * 
	 * @param buffer
	 * @return
	 */
	public static String toString(byte[] buffer) {
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
