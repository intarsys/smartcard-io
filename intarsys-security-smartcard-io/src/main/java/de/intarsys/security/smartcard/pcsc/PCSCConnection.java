/*
 * Copyright (c) 2013, intarsys GmbH
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * - Neither the name of intarsys nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific
 * prior written permission.
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

import static de.intarsys.security.smartcard.pcsc.PCSCTools.BufferHelper;

import java.util.concurrent.atomic.AtomicInteger;

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.NativeBuffer;
import de.intarsys.security.smartcard.pcsc.nativec.NativePcscDword;
import de.intarsys.security.smartcard.pcsc.nativec.SCARDHANDLE;
import de.intarsys.tools.exception.ExceptionTools;
import de.intarsys.tools.hex.HexTools;
import de.intarsys.tools.yalf.api.ILogger;
import de.intarsys.tools.yalf.api.Level;

/**
 * The default {@link IPCSCConnection } implementation.
 * 
 */
public class PCSCConnection implements IPCSCConnection {

	private static final ILogger Log = PACKAGE.Log;

	private static final AtomicInteger Counter = new AtomicInteger(0);

	private static final Object lockGetMapped = new Object();

	private final String id;

	private final CommonPCSCContext context;

	private final SCARDHANDLE hCard;

	private final INativeHandle protocolHandle;

	private final int shareMode;

	private final int protocol;

	private NativeBuffer sendBuffer;

	private NativeBuffer recvBuffer;

	private NativePcscDword nRecvLength;

	public PCSCConnection(CommonPCSCContext context, String id, SCARDHANDLE hCard, int shareMode, int protocol,
			INativeHandle protocolHandle) {
		this.id = id;
		this.context = context;
		this.hCard = hCard;
		this.shareMode = shareMode;
		this.protocol = protocol;
		this.protocolHandle = protocolHandle;
		int count = Counter.incrementAndGet();
		Log.debug("{} created, handle {}, share mode {}, protocol {}, {} active", getLogLabel(), Long.toHexString(
				hCard.longValue()), shareMode, protocol, count);
	}

	@Override
	public void beginTransaction() throws PCSCException {
		Log.trace("{} begin transaction", getLogLabel()); //$NON-NLS-1$
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
	@Override
	public byte[] control(int controlCode, byte[] inBuffer, int inBufferOffset, int inBufferLength, int outBufferSize)
			throws PCSCException {
		logBytes("control 0x" + Integer.toHexString(controlCode) + " request", //$NON-NLS-1$ //$NON-NLS-2$
				inBuffer, inBufferOffset, inBufferLength, false);
		if (inBuffer != null) {
			if (sendBuffer == null || sendBuffer.getSize() < inBufferLength) {
				int tempLength = Math.max(inBufferLength, 512);
				sendBuffer = new NativeBuffer(tempLength);
			}
			sendBuffer.setByteArray(0, inBuffer, inBufferOffset, inBufferLength);
		}
		if (recvBuffer == null || recvBuffer.getSize() < outBufferSize) {
			int tempLength = Math.max(outBufferSize, 4096);
			recvBuffer = new NativeBuffer(tempLength);
			nRecvLength = new NativePcscDword();
		}
		nRecvLength.setValue(outBufferSize);
		int rc = getContext().getPcsc().SCardControl(hCard, controlCode, sendBuffer, inBufferLength, recvBuffer,
				recvBuffer.getSize(), nRecvLength);
		int size = nRecvLength.intValue();
		PCSCException.checkReturnCode(rc, size);
		byte[] result = recvBuffer.getByteArray(0, size);
		logBytes("control 0x" + Integer.toHexString(controlCode) + " response", //$NON-NLS-1$ //$NON-NLS-2$
				result, 0, result.length, false);
		return result;
	}

	/**
	 * SCardControl with a prior mapping of control codes. This is because control
	 * codes are defined differently in Win* and PCSClite environments. This is
	 * further complicated by the fact that in a terminal server environment we may
	 * BELIEVE to use Win* but really need PCSClite control codes for accessing the
	 * card terminal.
	 * 
	 * @param code
	 * @param inBuffer
	 * @param inBufferOffset
	 * @param inBufferLength
	 * @param outBufferSize
	 * @return
	 * @throws PCSCException
	 */
	@Override
	public byte[] controlMapped(int code, byte[] inBuffer, int inBufferOffset, int inBufferLength, int outBufferSize)
			throws PCSCException {
		int controlCode = PCSCContextFactory.mapControlCode(code);
		// be sure this lock is static, multiple connections may fail in
		// controlMapped unexpectedly otherwise
		synchronized (lockGetMapped) {
			try {
				return control(controlCode, inBuffer, inBufferOffset, inBufferLength, outBufferSize);
			} catch (PCSCException e1) {
				if (PCSCContextFactory.isPcscLite()) {
					// we already use PCSCLite - no use in retrying
					Log.trace("{} control mapped request failed ({})", getLogLabel(), ExceptionTools.getMessage(e1));
					throw e1;
				}
				Log.debug("{} control mapped request failed - retry PCSC lite version", getLogLabel()); //$NON-NLS-1$
				// switch to PCSCLite and retry. This is necessary in a Citrix
				// environment with Unix/Mac based client
				PCSCContextFactory.setPcscLite(true);
				controlCode = PCSCContextFactory.mapControlCode(code);
				try {
					// if this is fine we return the result and let the
					// system stay in the PCSC lite state!
					return control(controlCode, inBuffer, inBufferOffset, inBufferLength, outBufferSize);
				} catch (PCSCException e2) {
					// bad luck if both fail, we must revert to windows...
					PCSCContextFactory.setPcscLite(false);
					// and fail with the initial exception
					Log.trace("{} control mapped request failed ({})", getLogLabel(), ExceptionTools.getMessage(e1));
					throw e1;
				}
			}
		}
	}

	@Override
	public void disconnect(int disposition) throws PCSCException {
		int count = Counter.decrementAndGet();
		Log.debug("{} disconnect {}, {} active", getLogLabel(), disposition, count); //$NON-NLS-1$
		getContext().fromConnectionDisconnect(this, disposition);
	}

	@Override
	public void endTransaction(int disposition) throws PCSCException {
		Log.trace("{} end transaction {}", getLogLabel(), disposition); //$NON-NLS-1$ //$NON-NLS-2$
		// inform the context for reference counting
		getContext().fromConnectionEndTransaction(this, disposition);
	}

	@Override
	public byte[] getAttrib(int attrId) throws PCSCException {
		Log.trace("{} get attrib {}", getLogLabel(), Integer.toHexString(attrId)); //$NON-NLS-1$
		byte[] result;
		try {
			result = BufferHelper.call(context,
					(buffer, bufferSize) -> getContext().getPcsc().SCardGetAttrib(hCard, attrId, buffer, bufferSize));
		} catch (PCSCException ex) {
			Log.trace("{} get attrib {}, exception {}", getLogLabel(), attrId, ex.getErrorCode()); //$NON-NLS-1$
			return new byte[0];
		}
		logBytes("attrib 0x" + Integer.toHexString(attrId) + " response", //$NON-NLS-1$ //$NON-NLS-2$
				result, 0, result.length, false);
		return result;
	}

	@Override
	public CommonPCSCContext getContext() {
		return context;
	}

	protected SCARDHANDLE getHCard() {
		return hCard;
	}

	@Override
	public String getId() {
		return id;
	}

	protected String getLogLabel() {
		return "PCSCConnection " + getId(); //$NON-NLS-1$
	}

	@Override
	public int getProtocol() {
		return protocol;
	}

	@Override
	public int getShareMode() {
		return shareMode;
	}

	@Override
	public CardStatus getStatus() throws PCSCException {
		/*
		 * this pollutes the log
		 */
		// Log.trace("{} get status", getLogLabel()); //$NON-NLS-1$
		NativePcscDword nativeState = new NativePcscDword();
		NativePcscDword nativeProtocol = new NativePcscDword();
		int atrSize = 32;
		NativeBuffer nativeAtr = new NativeBuffer(atrSize);
		NativePcscDword nativeAtrSize = new NativePcscDword(atrSize);
		byte[] result = BufferHelper.call(context, (buffer, bufferSize) -> getContext().getPcsc().SCardStatus(hCard,
				buffer, bufferSize, nativeState, nativeProtocol, nativeAtr, nativeAtrSize));
		return new CardStatus(new String(result).split("\0"), nativeState.intValue(), nativeProtocol.intValue(),
				nativeAtr.getByteArray(0, nativeAtrSize.intValue()));
	}

	protected void logBytes(String mode, byte[] bytes, int offset, int length, boolean sensitiveContent) {
		if (Log.isLoggable(Level.TRACE)) {
			if (sensitiveContent) {
				Log.trace("" //$NON-NLS-1$
						+ getLogLabel() + " " //$NON-NLS-1$
						+ mode + "[" //$NON-NLS-1$
						+ length + "]: " //$NON-NLS-1$
						+ (bytes == null ? "" //$NON-NLS-1$
								: HexTools.bytesToHexString(bytes, offset, 4, true))
						+ " <sensitive content omitted>"); //$NON-NLS-1$
			} else {
				Log.trace("" //$NON-NLS-1$
						+ getLogLabel() + " " //$NON-NLS-1$
						+ mode + "[" //$NON-NLS-1$
						+ length + "]: " //$NON-NLS-1$
						+ (bytes == null ? "" //$NON-NLS-1$
								: HexTools.bytesToHexString(bytes, offset, length, true)));
			}
		}
	}

	@Override
	public void reconnect(int shareMode, int protocol, int initialization) throws PCSCException {
		Log.trace("{} reconnect {}, {}, {}", getLogLabel(), shareMode, protocol, initialization); //$NON-NLS-1$ //$NON-NLS-2$
		NativePcscDword activeProtocol = new NativePcscDword();
		int rc = getContext().getPcsc().SCardReconnect(hCard, shareMode, protocol, initialization, activeProtocol);
		PCSCException.checkReturnCode(rc);
	}

	@Override
	public String toString() {
		return getLogLabel();
	}

	@Override
	public byte[] transmit(byte[] apdu, int apduOffset, int apduLength, int recvLength, boolean sensitiveContent)
			throws PCSCException {
		logBytes("transmit", apdu, apduOffset, apduLength, sensitiveContent); //$NON-NLS-1$
		if (sendBuffer == null || sendBuffer.getSize() < apduLength) {
			int tempLength = Math.max(apduLength, 512);
			sendBuffer = new NativeBuffer(tempLength);
		}
		sendBuffer.setByteArray(0, apdu, apduOffset, apduLength);
		if (recvBuffer == null || recvBuffer.getSize() < recvLength) {
			int tempLength = Math.max(recvLength, 4096);
			recvBuffer = new NativeBuffer(tempLength);
			nRecvLength = new NativePcscDword();
		}
		nRecvLength.setValue(recvLength);
		int rc = getContext().getPcsc().SCardTransmit(hCard, protocolHandle, sendBuffer, apduLength, null, recvBuffer,
				nRecvLength);
		PCSCException.checkReturnCode(rc);
		int responseSize = nRecvLength.intValue();
		byte[] result = recvBuffer.getByteArray(0, responseSize);
		logBytes("receive", result, 0, result.length, false); //$NON-NLS-1$
		return result;
	}
}
