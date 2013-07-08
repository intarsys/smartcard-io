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

import java.util.logging.Level;
import java.util.logging.Logger;

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.NativeBuffer;
import de.intarsys.security.smartcard.pcsc.nativec.NativePcscDword;
import de.intarsys.security.smartcard.pcsc.nativec.SCARDHANDLE;
import de.intarsys.tools.hex.HexTools;

/**
 * The representation of a PCSC SCardHandle.
 * 
 */
public class PCSCConnection {

	private static final Logger Log = PACKAGE.Log;

	final private CommonPCSCContext context;

	final private SCARDHANDLE hCard;

	private Object lockGetMapped = new Object();

	final private INativeHandle protocolHandle;

	final private int shareMode;

	final private int protocol;

	private NativeBuffer sendBuffer;

	private NativeBuffer recvBuffer;

	private NativePcscDword nRecvLength;

	public PCSCConnection(CommonPCSCContext context, SCARDHANDLE hCard,
			int shareMode, int protocol, INativeHandle protocolHandle) {
		this.context = context;
		this.hCard = hCard;
		this.shareMode = shareMode;
		this.protocol = protocol;
		this.protocolHandle = protocolHandle;
	}

	public void beginTransaction() throws PCSCException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " begin transaction"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		getContext().fromConnectionBeginTransaction(this);
	}

	/**
	 * SCardControl
	 * 
	 * @param controlCode
	 * @param inBuffer
	 * @param inBufferOffset
	 * @param inBufferLength
	 * @param outBufferSize
	 * @return
	 * @throws PCSCException
	 */
	public byte[] control(int controlCode, byte[] inBuffer, int inBufferOffset,
			int inBufferLength, int outBufferSize) throws PCSCException {
		logBytes("control 0x" + Integer.toHexString(controlCode) + " request", //$NON-NLS-1$ //$NON-NLS-2$
				inBuffer, inBufferOffset, inBufferLength, false);
		NativeBuffer nInBuffer = null;
		if (inBuffer != null) {
			nInBuffer = new NativeBuffer(inBufferLength);
			nInBuffer.setByteArray(0, inBuffer, inBufferOffset, inBufferLength);
		}
		NativeBuffer outBuffer = new NativeBuffer(outBufferSize);
		NativePcscDword bytesReturned = new NativePcscDword();

		int rc = getContext().getPcsc().SCardControl(hCard, controlCode,
				nInBuffer, inBufferLength, outBuffer, outBuffer.getSize(),
				bytesReturned);
		PCSCTools.checkReturnCode(rc);
		int size = bytesReturned.intValue();
		byte[] result = outBuffer.getByteArray(0, size);
		logBytes("control 0x" + Integer.toHexString(controlCode) + " response", //$NON-NLS-1$ //$NON-NLS-2$
				result, 0, result.length, false);
		return result;
	}

	/**
	 * SCardControl with a prior mapping of control codes. This is because
	 * control codes are defined differently in Win* and PCSClite environments.
	 * This is further complicated by the fact that in a terminal server
	 * environment we may BELIEVE to use Win* but really need PCSClite control
	 * codes for accessing the card terminal.
	 * 
	 * @param code
	 * @param inBuffer
	 * @param inBufferOffset
	 * @param inBufferLength
	 * @param outBufferSize
	 * @return
	 * @throws PCSCException
	 */
	public byte[] controlMapped(int code, byte[] inBuffer, int inBufferOffset,
			int inBufferLength, int outBufferSize) throws PCSCException {
		int controlCode = PCSCTools.mapControlCode(code);
		synchronized (lockGetMapped) {
			try {
				return control(controlCode, inBuffer, inBufferOffset,
						inBufferLength, outBufferSize);
			} catch (PCSCException e1) {
				if (PCSCTools.isPcscLite()) {
					// we already use PCSCLite - no use in retrying
					Log.log(Level.INFO,
							"" //$NON-NLS-1$
									+ this
									+ " control mapped request failed with return code " //$NON-NLS-1$
									+ e1.getLocalizedMessage());
					throw e1;
				}
				Log.log(Level.FINE,
						"" //$NON-NLS-1$
								+ this
								+ " control mapped request failed - retry PCSC lite version"); //$NON-NLS-1$
				// switch to PCSCLite and retry. This is necessary in a Citrix
				// environment with Unix/Mac based client
				PCSCTools.setPcscLite(true);
				controlCode = PCSCTools.mapControlCode(code);
				try {
					// if this is fine we return the result and let the
					// system stay in the PCSC lite state!
					return control(controlCode, inBuffer, inBufferOffset,
							inBufferLength, outBufferSize);
				} catch (PCSCException e2) {
					// bad luck if both fail, we must revert to windows...
					PCSCTools.setPcscLite(false);
					// and fail with the initial exception
					Log.log(Level.INFO,
							"" //$NON-NLS-1$
									+ this
									+ " control mapped request failed with return code " //$NON-NLS-1$
									+ e1.getLocalizedMessage());
					throw e1;
				}
			}
		}
	}

	public void disconnect(int disposition) throws PCSCException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " disconnect"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// inform the context for reference counting
		getContext().fromConnectionDisconnect(this, disposition);
	}

	public void endTransaction(int disposition) throws PCSCException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " end transaction"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// inform the context for reference counting
		getContext().fromConnectionEndTransaction(this, disposition);
	}

	public byte[] getAttrib(int attrId) throws PCSCException {
		if (Log.isLoggable(Level.FINEST)) {
			Log.finest("" + this + " get attrib " + attrId); //$NON-NLS-1$ //$NON-NLS-2$
		}
		NativePcscDword bufferSize = new NativePcscDword(0);
		int rc = getContext().getPcsc().SCardGetAttrib(hCard, attrId, null,
				bufferSize);
		try {
			PCSCTools.checkReturnCode(rc);
		} catch (PCSCException ex) {
			if (Log.isLoggable(Level.FINEST)) {
				Log.finest("" + this + " get attrib " + attrId + " exception " + ex.getErrorCode()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return new byte[0];
		}

		NativeBuffer outBuffer = new NativeBuffer(bufferSize.intValue());
		rc = getContext().getPcsc().SCardGetAttrib(hCard, attrId, outBuffer,
				bufferSize);
		PCSCTools.checkReturnCode(rc);
		byte[] result = outBuffer.getByteArray(0, bufferSize.intValue());
		logBytes("attrib 0x" + Integer.toHexString(attrId) + " response", //$NON-NLS-1$ //$NON-NLS-2$
				result, 0, result.length, false);
		return result;
	}

	public CommonPCSCContext getContext() {
		return context;
	}

	protected SCARDHANDLE getHCard() {
		return hCard;
	}

	public int getProtocol() {
		return protocol;
	}

	public int getShareMode() {
		return shareMode;
	}

	public void getStatus() throws PCSCException {
		int rc = getContext().getPcsc().SCardStatus(hCard, null, null, null,
				null, null, null);
		PCSCTools.checkReturnCode(rc);
	}

	protected void logBytes(String mode, byte[] bytes, int offset, int length,
			boolean sensitiveContent) {
		if (Log.isLoggable(Level.FINEST)) {
			if (sensitiveContent) {
				Log.finest("" //$NON-NLS-1$
						+ this
						+ " " //$NON-NLS-1$
						+ mode
						+ "[" //$NON-NLS-1$
						+ length
						+ "]: " //$NON-NLS-1$
						+ (bytes == null ? "" : HexTools.bytesToHexString( //$NON-NLS-1$
								bytes, offset, 4, true))
						+ " <sensitive content omitted>"); //$NON-NLS-1$
			} else {
				Log.finest("" //$NON-NLS-1$
						+ this + " " //$NON-NLS-1$
						+ mode + "[" //$NON-NLS-1$
						+ length + "]: " //$NON-NLS-1$
						+ (bytes == null ? "" : HexTools.bytesToHexString( //$NON-NLS-1$
								bytes, offset, length, true)));
			}
		}
	}

	public void reconnect(int shareMode, int protocol, int initialization)
			throws PCSCException {
		NativePcscDword activeProtocol = new NativePcscDword();
		int rc = getContext().getPcsc().SCardReconnect(hCard, shareMode,
				protocol, initialization, activeProtocol);
		PCSCTools.checkReturnCode(rc);
	}

	@Override
	public String toString() {
		return "PCSC connection " + Long.toHexString(hCard.longValue()); //$NON-NLS-1$
	}

	public byte[] transmit(byte[] apdu, int apduOffset, int apduLength,
			int recvLength, boolean sensitiveContent) throws PCSCException {
		logBytes("transmit", apdu, apduOffset, apduLength, sensitiveContent); //$NON-NLS-1$
		if (sendBuffer == null || sendBuffer.getSize() < apduLength) {
			int tempLength = Math.max(apduLength, 512);
			sendBuffer = new NativeBuffer(tempLength);
		}
		sendBuffer.setByteArray(0, apdu, apduOffset, apduLength);
		if (recvBuffer == null || recvBuffer.getSize() < apduLength) {
			int tempLength = Math.max(apduLength, 512);
			recvBuffer = new NativeBuffer(tempLength);
			nRecvLength = new NativePcscDword();
		}
		nRecvLength.setValue(recvLength);
		int rc = getContext().getPcsc().SCardTransmit(hCard, protocolHandle,
				sendBuffer, apduLength, null, recvBuffer, nRecvLength);
		PCSCTools.checkReturnCode(rc);
		int responseSize = nRecvLength.intValue();
		byte[] result = recvBuffer.getByteArray(0, responseSize);
		logBytes("receive", result, 0, result.length, false); //$NON-NLS-1$
		return result;
	}
}
