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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.intarsys.nativec.api.INativeHandle;
import de.intarsys.nativec.type.INativeObject;
import de.intarsys.nativec.type.NativeBuffer;
import de.intarsys.nativec.type.NativeLongLP64;
import de.intarsys.nativec.type.NativeString;
import de.intarsys.tools.concurrent.ThreadTools;
import de.intarsys.tools.exception.ExceptionTools;

public class _PCSCThreadedExecutor implements _IPCSC {

	private final _IPCSC pcsc;

	private final ExecutorService pcscExecutor;

	public _PCSCThreadedExecutor(_IPCSC pcsc) {
		this.pcsc = pcsc;
		pcscExecutor = Executors.newCachedThreadPool(ThreadTools
				.newThreadFactoryDaemon("pcsc worker queue")); //$NON-NLS-1$
	}

	protected _IPCSC getPcsc() {
		return pcsc;
	}

	@Override
	public INativeHandle getSCARD_PCI_RAW() {
		Callable<INativeHandle> callCallable;
		Future<INativeHandle> callFuture;

		callCallable = new Callable<INativeHandle>() {
			@Override
			public INativeHandle call() throws Exception {
				return getPcsc().getSCARD_PCI_RAW();
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGet(callFuture);
	}

	@Override
	public INativeHandle getSCARD_PCI_T0() {
		Callable<INativeHandle> callCallable;
		Future<INativeHandle> callFuture;

		callCallable = new Callable<INativeHandle>() {
			@Override
			public INativeHandle call() throws Exception {
				return getPcsc().getSCARD_PCI_T0();
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGet(callFuture);
	}

	@Override
	public INativeHandle getSCARD_PCI_T1() {
		Callable<INativeHandle> callCallable;
		Future<INativeHandle> callFuture;

		callCallable = new Callable<INativeHandle>() {
			@Override
			public INativeHandle call() throws Exception {
				return getPcsc().getSCARD_PCI_T1();
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGet(callFuture);
	}

	@Override
	public int SCardBeginTransaction(final SCARDHANDLE hCard) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardBeginTransaction(hCard);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardCancel(final SCARDCONTEXT hContext) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardCancel(hContext);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardConnect(final SCARDCONTEXT context,
			final NativeString szReader, final long dwShareMode,
			final long dwPreferredProtocols, final NativeLongLP64 phCard,
			final NativePcscDword pdwActiveProtocol) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardConnect(context, szReader, dwShareMode,
						dwPreferredProtocols, phCard, pdwActiveProtocol);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardControl(final SCARDHANDLE card, final long dwControlCode,
			final NativeBuffer lpInBuffer, final long inBufferSize,
			final NativeBuffer lpOutBuffer, final long outBufferSize,
			final NativePcscDword lpBytesReturned) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardControl(card, dwControlCode, lpInBuffer,
						inBufferSize, lpOutBuffer, outBufferSize,
						lpBytesReturned);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardDisconnect(final SCARDHANDLE card, final long dwDisposition) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardDisconnect(card, dwDisposition);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardEndTransaction(final SCARDHANDLE card,
			final long dwDisposition) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardEndTransaction(card, dwDisposition);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardEstablishContext(final long dwScope,
			final NativeLongLP64 phContext) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardEstablishContext(dwScope, phContext);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardGetAttrib(final SCARDHANDLE hCard, final long dwAttrId,
			final NativeBuffer pbAttr, final NativePcscDword pcbAttrLength) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardGetAttrib(hCard, dwAttrId, pbAttr,
						pcbAttrLength);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardGetStatusChange(final SCARDCONTEXT context,
			final long dwTimeout, final INativeObject rgReaderStates,
			final long readers) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardGetStatusChange(context, dwTimeout,
						rgReaderStates, readers);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardListReaders(final SCARDCONTEXT context,
			final NativeString mszGroups, final INativeObject mszReaders,
			final NativePcscDword pcchReaders) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardListReaders(context, mszGroups,
						mszReaders, pcchReaders);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardReconnect(final SCARDHANDLE hCard, final long dwShareMode,
			final long dwPreferredProtocols, final long dwInitialization,
			final NativePcscDword pdwActiveProtocol) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardReconnect(hCard, dwShareMode,
						dwPreferredProtocols, dwInitialization,
						pdwActiveProtocol);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardReleaseContext(final SCARDCONTEXT context) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardReleaseContext(context);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardStatus(final SCARDHANDLE hCard,
			final INativeObject szReaderName,
			final NativePcscDword pcchReaderLen,
			final NativePcscDword pdwState, final NativePcscDword pdwProtocol,
			final NativeBuffer pbAtr, final NativePcscDword pcbAtrLen) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardStatus(hCard, szReaderName,
						pcchReaderLen, pdwState, pdwProtocol, pbAtr, pcbAtrLen);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

	@Override
	public int SCardTransmit(final SCARDHANDLE card,
			final INativeHandle pioSendPci, final INativeObject pbSendBuffer,
			final long cbSendLength, final INativeObject pioRecvPci,
			final NativeBuffer pbRecvBuffer, final NativePcscDword pcbRecvLength) {
		Callable<Integer> callCallable;
		Future<Integer> callFuture;

		callCallable = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return getPcsc().SCardTransmit(card, pioSendPci, pbSendBuffer,
						cbSendLength, pioRecvPci, pbRecvBuffer, pcbRecvLength);
			}
		};
		callFuture = pcscExecutor.submit(callCallable);
		return ExceptionTools.futureSimpleGetNumber(callFuture);
	}

}
