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
package de.intarsys.security.smartcard.card;

/**
 * A simple transmittter implementing command chaining.
 * 
 */
public class RequestChainingCardTransmitter extends CommonCardTransmitter {

	public RequestChainingCardTransmitter(ICardTransmitter transmitter) {
		super(transmitter);
	}

	protected RequestAPDU fixLengthExpected(RequestAPDU request, int correctLE) {
		int cla = request.getCla();
		int ins = request.getIns();
		int p1 = request.getP1();
		int p2 = request.getP2();
		byte[] data = request.getData();
		if (data == null) {
			return new RequestAPDU(cla, ins, p1, p2, correctLE);
		} else {
			return new RequestAPDU(cla, ins, p1, p2, data, correctLE);
		}
	}

	@Override
	public ResponseAPDU transmit(RequestAPDU request) throws CardException {
		ResponseAPDU response = super.transmit(request);
		if (response.getSw1() == 0x6C) {
			// wrong LengthExpected field: happens e.g. on ReinerSCT e-com in
			// combination with Starcos3.0 cards
			int le = response.getSw2();
			RequestAPDU fixedRequest = fixLengthExpected(request, le);
			response = super.transmit(fixedRequest);
		}
		if (response.getSw1() == 0x61) {
			// "GET RESPONSE" command
			RequestAPDU fixedRequest = new RequestAPDU(0, 0xC0, 0, 0,
					response.getSw2());
			response = super.transmit(fixedRequest);
		}
		if (request.isChainedRequest() && request.getNextRequest() != null) {
			response = transmit(request.getNextRequest());
		}
		return response;
	}

}
