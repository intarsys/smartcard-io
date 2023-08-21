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

/**
 * A simple transmittter for T0 protocol.
 * 
 * T0 specific code as described in
 * http://www.cardwerk.com/smartcards/smartcard_standard_ISO7816-4_annex-a. aspx
 * 
 */
public class T0CardTransmitter extends CommonCardTransmitter {

	class T0Request {
		private RequestAPDU request;
		private byte[] bytes;
		private int offset = 0;
		private boolean ready = false;
		private boolean envelope = false;

		public T0Request(RequestAPDU request) {
			super();
			convert(request);
		}

		protected void convert(RequestAPDU request) {
			this.request = request;
			byte p3 = 0x00;
			int lc = request.getLc();
			int le = request.getLe();
			if (lc == -1 && le == -1) {
				// case 1
				p3 = 0x00;
			} else if (lc == -1 && le >= 0) {
				// case 2
				if (le <= 256) {
					p3 = (byte) le;
				} else {
					p3 = 0x00;
				}
			} else if (lc >= 0 && le == -1) {
				// case 3
				if (lc < 256) {
					p3 = (byte) lc;
				} else {
					// use CLA ENVELOPE
					envelope = true;
				}
			} else {
				// case 4
				if (lc < 256) {
					p3 = (byte) lc;
				} else {
					// use CLA ENVELOPE
					envelope = true;
				}
			}
			int length = 5;
			if (request.getData() != null) {
				length += request.getData().length;
			}
			if (envelope) {
				bytes = request.getBytes();
			} else {
				bytes = new byte[length];
				bytes[0] = (byte) request.getCla();
				bytes[1] = (byte) request.getIns();
				bytes[2] = (byte) request.getP1();
				bytes[3] = (byte) request.getP2();
				bytes[4] = p3;
				if (request.getData() != null) {
					System.arraycopy(request.getData(), 0, bytes, 5, request.getData().length);
				}
			}
		}

		protected RequestAPDU createRequest() {
			if (envelope) {
				int len = Math.min(256, bytes.length);
				byte[] chunk = new byte[len];
				System.arraycopy(bytes, offset, chunk, 0, chunk.length);
				offset += len;
				if (offset >= bytes.length) {
					ready = true;
				}
				return new RequestAPDU(request.getCla(), INS_ENVELOPE, 0, 0, chunk, -1, false);
			} else {
				ready = true;
				return new RequestAPDU(bytes);
			}
		}

		public boolean isReady() {
			return ready;
		}

		public ResponseAPDU transmit() throws CardException {
			RequestAPDU tRequest = createRequest();
			ResponseAPDU response = basicTransmit(tRequest);
			if (response.getSw() == 0x9000 && request.getLe() > 0 && response.getBytes().length == 2) {
				// "GET RESPONSE" command
				RequestAPDU getReq = new RequestAPDU(request.getCla(), INS_GET_RESPONSE, 0, 0, request.getLe(), false);
				response = basicTransmit(getReq);
			}
			// wrong length
			if (response.getSw1() == 0x6C) {
				int le = response.getSw2();
				convert(fixLengthExpected(request, le));
				offset = 0;
				RequestAPDU fixedRequest = createRequest();
				response = basicTransmit(fixedRequest);
			}
			// more available
			if (response.getSw1() == 0x61) {
				int len = response.getSw2();
				RequestAPDU fixedRequest = new RequestAPDU(request.getCla(), INS_GET_RESPONSE, 0, 0, len, false);
				response = basicTransmit(fixedRequest);
			}
			return response;
		}

	}

	protected static final int INS_GET_RESPONSE = 0xC0;

	protected static final int INS_ENVELOPE = 0xC2;

	public T0CardTransmitter(ICardTransmitter transmitter) {
		super(transmitter);
	}

	@Override
	public ResponseAPDU transmit(RequestAPDU request) throws CardException {
		ResponseAPDU response = null;
		T0Request t0Request = new T0Request(request);
		while (!t0Request.isReady()) {
			response = t0Request.transmit();
		}
		return response;
	}

}
