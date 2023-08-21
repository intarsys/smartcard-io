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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import de.intarsys.tools.hex.HexTools;

/**
 * The data object received from the {@link ICard}.
 * <p>
 * See ISO 7816-4
 * 
 */
public class ResponseAPDU {

	private final byte[] bytes;

	public ResponseAPDU(byte[] response) throws CardException {
		assert (response != null);
		if (response.length < 2) {
			throw new CardException("Invalid response received from card reader"); //$NON-NLS-1$
		}
		this.bytes = response;
	}

	public ResponseAPDU(byte[] data, int sw) {
		if (data == null) {
			data = new byte[0];
		}
		bytes = new byte[data.length + 2];
		System.arraycopy(data, 0, bytes, 0, data.length);
		bytes[data.length] = (byte) ((sw >> 8) & 0xff);
		bytes[data.length + 1] = (byte) (sw & 0xff);
	}

	public ResponseAPDU(byte[] data, int sw1, int sw2) {
		if (data == null) {
			data = new byte[0];
		}
		bytes = new byte[data.length + 2];
		System.arraycopy(data, 0, bytes, 0, data.length);
		bytes[data.length] = (byte) sw1;
		bytes[data.length + 1] = (byte) sw2;
	}

	/**
	 * The whole APDU content including SW.
	 * 
	 * @return The whole APDU content including SW.
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * The net data of the APDU.
	 * 
	 * @return The net data of the APDU.
	 */
	public byte[] getData() {
		byte[] data = new byte[bytes.length - 2];
		System.arraycopy(bytes, 0, data, 0, data.length);
		return data;
	}

	/**
	 * Fill length bytes starting at offset of the net data of the APDU into
	 * pBytes. The number of bytes really transferred are returned.
	 * 
	 * @param pBytes
	 * @param offset
	 * @param length
	 * @return The number of bytes copied.
	 */
	public int getData(byte[] pBytes, int offset, int length) {
		int count = bytes.length - 2;
		if (length < count) {
			count = length;
		}
		System.arraycopy(bytes, 0, pBytes, offset, count);
		return count;
	}

	/**
	 * The net data of the APDU as an {@link InputStream}.
	 * 
	 * @return
	 */
	public InputStream getInputStream() {
		return new ByteArrayInputStream(bytes, 0, bytes.length - 2);
	}

	/**
	 * The two SW bytes as an int (MSB first).
	 * 
	 * @return
	 */
	public int getSw() {
		return ((bytes[bytes.length - 2] & 0xFF) << 8) + (bytes[bytes.length - 1] & 0xFF);
	}

	/**
	 * First byte of SW.
	 * 
	 * @return
	 */
	public int getSw1() {
		return bytes[bytes.length - 2] & 0xFF;
	}

	/**
	 * Second byte of SW.
	 * 
	 * @return
	 */
	public int getSw2() {
		return bytes[bytes.length - 1] & 0xFF;
	}

	public String getSwString() {
		return "0x" + Integer.toHexString(getSw1()) + "" + Integer.toHexString(getSw2());
	}

	/**
	 * True if APDU contains net data.
	 * 
	 * @return
	 * 
	 */
	public boolean hasData() {
		return bytes.length > 2;
	}

	/**
	 * True if SW code == 9000
	 * 
	 * @return
	 */
	public boolean isOk() {
		return getSw1() == 0x90 && getSw2() == 0x00;
	}

	@Override
	public String toString() {
		return HexTools.bytesToHexString(bytes);
	}

}
