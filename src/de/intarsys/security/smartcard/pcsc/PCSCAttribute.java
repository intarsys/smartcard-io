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

public class PCSCAttribute {

	// all constants imported from WinSmCrd.h

	public static final int SCARD_CLASS_VENDOR_INFO = 1;
	public static final int SCARD_CLASS_COMMUNICATIONS = 2;
	public static final int SCARD_CLASS_PROTOCOL = 3;
	public static final int SCARD_CLASS_POWER_MGMT = 4;
	public static final int SCARD_CLASS_SECURITY = 5;
	public static final int SCARD_CLASS_MECHANICAL = 6;
	public static final int SCARD_CLASS_VENDOR_DEFINED = 7;
	public static final int SCARD_CLASS_IFD_PROTOCOL = 8;
	public static final int SCARD_CLASS_ICC_STATE = 9;
	public static final int SCARD_CLASS_PERF = 0x7ffe;
	public static final int SCARD_CLASS_SYSTEM = 0x7fff;

	public static final int SCARD_ATTR_VENDOR_NAME = SCARD_ATTR_VALUE(
			SCARD_CLASS_VENDOR_INFO, 0x0100);

	public static final int SCARD_ATTR_VENDOR_IFD_TYPE = SCARD_ATTR_VALUE(
			SCARD_CLASS_VENDOR_INFO, 0x0101);
	/**
	 * Vendor-supplied interface device version (DWORD in the form 0xMMmmbbbb
	 * where MM = major version, mm = minor version, and bbbb = build number).
	 */
	public static final int SCARD_ATTR_VENDOR_IFD_VERSION = SCARD_ATTR_VALUE(
			SCARD_CLASS_VENDOR_INFO, 0x0102);
	public static final int SCARD_ATTR_VENDOR_IFD_SERIAL_NO = SCARD_ATTR_VALUE(
			SCARD_CLASS_VENDOR_INFO, 0x0103);
	public static final int SCARD_ATTR_CHANNEL_ID = SCARD_ATTR_VALUE(
			SCARD_CLASS_COMMUNICATIONS, 0x0110);
	public static final int SCARD_ATTR_PROTOCOL_TYPES = SCARD_ATTR_VALUE(
			SCARD_CLASS_PROTOCOL, 0x0120);

	public static final int SCARD_ATTR_DEFAULT_CLK = SCARD_ATTR_VALUE(
			SCARD_CLASS_PROTOCOL, 0x0121);
	public static final int SCARD_ATTR_MAX_CLK = SCARD_ATTR_VALUE(
			SCARD_CLASS_PROTOCOL, 0x0122);
	public static final int SCARD_ATTR_DEFAULT_DATA_RATE = SCARD_ATTR_VALUE(
			SCARD_CLASS_PROTOCOL, 0x0123);
	public static final int SCARD_ATTR_MAX_DATA_RATE = SCARD_ATTR_VALUE(
			SCARD_CLASS_PROTOCOL, 0x0124);
	public static final int SCARD_ATTR_MAX_IFSD = SCARD_ATTR_VALUE(
			SCARD_CLASS_PROTOCOL, 0x0125);

	public static final int SCARD_ATTR_POWER_MGMT_SUPPORT = SCARD_ATTR_VALUE(
			SCARD_CLASS_POWER_MGMT, 0x0131);
	public static final int SCARD_ATTR_USER_TO_CARD_AUTH_DEVICE = SCARD_ATTR_VALUE(
			SCARD_CLASS_SECURITY, 0x0140);
	public static final int SCARD_ATTR_USER_AUTH_INPUT_DEVICE = SCARD_ATTR_VALUE(
			SCARD_CLASS_SECURITY, 0x0142);
	public static final int SCARD_ATTR_CHARACTERISTICS = SCARD_ATTR_VALUE(
			SCARD_CLASS_MECHANICAL, 0x0150);

	public static final int SCARD_ATTR_CURRENT_PROTOCOL_TYPE = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x0201);
	public static final int SCARD_ATTR_CURRENT_CLK = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x0202);
	public static final int SCARD_ATTR_CURRENT_F = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x0203);
	public static final int SCARD_ATTR_CURRENT_D = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x0204);
	public static final int SCARD_ATTR_CURRENT_N = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x0205);
	public static final int SCARD_ATTR_CURRENT_W = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x0206);
	public static final int SCARD_ATTR_CURRENT_IFSC = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x0207);
	public static final int SCARD_ATTR_CURRENT_IFSD = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x0208);
	public static final int SCARD_ATTR_CURRENT_BWT = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x0209);
	public static final int SCARD_ATTR_CURRENT_CWT = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x020a);
	public static final int SCARD_ATTR_CURRENT_EBC_ENCODING = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x020b);
	public static final int SCARD_ATTR_EXTENDED_BWT = SCARD_ATTR_VALUE(
			SCARD_CLASS_IFD_PROTOCOL, 0x020c);

	public static final int SCARD_ATTR_ICC_PRESENCE = SCARD_ATTR_VALUE(
			SCARD_CLASS_ICC_STATE, 0x0300);
	public static final int SCARD_ATTR_ICC_INTERFACE_STATUS = SCARD_ATTR_VALUE(
			SCARD_CLASS_ICC_STATE, 0x0301);
	public static final int SCARD_ATTR_CURRENT_IO_STATE = SCARD_ATTR_VALUE(
			SCARD_CLASS_ICC_STATE, 0x0302);
	public static final int SCARD_ATTR_ATR_STRING = SCARD_ATTR_VALUE(
			SCARD_CLASS_ICC_STATE, 0x0303);
	public static final int SCARD_ATTR_ICC_TYPE_PER_ATR = SCARD_ATTR_VALUE(
			SCARD_CLASS_ICC_STATE, 0x0304);

	public static final int SCARD_ATTR_ESC_RESET = SCARD_ATTR_VALUE(
			SCARD_CLASS_VENDOR_DEFINED, 0xA000);
	public static final int SCARD_ATTR_ESC_CANCEL = SCARD_ATTR_VALUE(
			SCARD_CLASS_VENDOR_DEFINED, 0xA003);
	public static final int SCARD_ATTR_ESC_AUTHREQUEST = SCARD_ATTR_VALUE(
			SCARD_CLASS_VENDOR_DEFINED, 0xA005);
	public static final int SCARD_ATTR_MAXINPUT = SCARD_ATTR_VALUE(
			SCARD_CLASS_VENDOR_DEFINED, 0xA007);

	public static final int SCARD_ATTR_DEVICE_UNIT = SCARD_ATTR_VALUE(
			SCARD_CLASS_SYSTEM, 0x0001);
	public static final int SCARD_ATTR_DEVICE_IN_USE = SCARD_ATTR_VALUE(
			SCARD_CLASS_SYSTEM, 0x0002);
	public static final int SCARD_ATTR_DEVICE_FRIENDLY_NAME_A = SCARD_ATTR_VALUE(
			SCARD_CLASS_SYSTEM, 0x0003);
	public static final int SCARD_ATTR_DEVICE_SYSTEM_NAME_A = SCARD_ATTR_VALUE(
			SCARD_CLASS_SYSTEM, 0x0004);
	public static final int SCARD_ATTR_DEVICE_FRIENDLY_NAME_W = SCARD_ATTR_VALUE(
			SCARD_CLASS_SYSTEM, 0x0005);
	public static final int SCARD_ATTR_DEVICE_SYSTEM_NAME_W = SCARD_ATTR_VALUE(
			SCARD_CLASS_SYSTEM, 0x0006);
	public static final int SCARD_ATTR_SUPRESS_T1_IFS_REQUEST = SCARD_ATTR_VALUE(
			SCARD_CLASS_SYSTEM, 0x0007);

	public static final int SCARD_PERF_NUM_TRANSMISSIONS = SCARD_ATTR_VALUE(
			SCARD_CLASS_PERF, 0x0001);
	public static final int SCARD_PERF_BYTES_TRANSMITTED = SCARD_ATTR_VALUE(
			SCARD_CLASS_PERF, 0x0002);
	public static final int SCARD_PERF_TRANSMISSION_TIME = SCARD_ATTR_VALUE(
			SCARD_CLASS_PERF, 0x0003);

	private static int SCARD_ATTR_VALUE(int attrClass, int attrTag) {
		return (attrClass << 16) | (attrTag);
	}
}
