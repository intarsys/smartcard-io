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

import de.intarsys.security.smartcard.pcsc.PCSCCardReset;
import de.intarsys.security.smartcard.pcsc.PCSCCardUnavailable;
import de.intarsys.security.smartcard.pcsc.PCSCSharingViolation;

/**
 * An exception in the card abstraction layer.
 */
public class CardException extends Exception {

	private static final long serialVersionUID = 1L;

	public static CardException create(String message, Throwable e) {
		if (e instanceof CardException) {
			return (CardException) e;
		}
		if (e instanceof PCSCCardReset) {
			return new CardReset();
		}
		if (e instanceof PCSCSharingViolation) {
			return new CardSharingViolation();
		}
		if (e instanceof PCSCCardUnavailable) {
			return new CardUnavailable(e.getMessage(), e);
		}
		return new CardException(message, e);
	}

	public static CardException create(Throwable e) {
		if (e instanceof CardException) {
			return (CardException) e;
		}
		if (e instanceof PCSCCardReset) {
			return new CardReset();
		}
		if (e instanceof PCSCSharingViolation) {
			return new CardSharingViolation();
		}
		if (e instanceof PCSCCardUnavailable) {
			return new CardUnavailable(e.getMessage(), e);
		}
		return new CardException(e.getLocalizedMessage(), e);
	}

	public CardException() {
		super();
	}

	public CardException(String message) {
		super(message);
	}

	public CardException(String message, Throwable cause) {
		super(message, cause);
	}

}
