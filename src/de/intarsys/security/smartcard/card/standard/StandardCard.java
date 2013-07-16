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
package de.intarsys.security.smartcard.card.standard;

import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import de.intarsys.security.smartcard.card.ATR;
import de.intarsys.security.smartcard.card.CardException;
import de.intarsys.security.smartcard.card.CardSharingViolationException;
import de.intarsys.security.smartcard.card.CommonCard;
import de.intarsys.security.smartcard.card.CommonCardConnection;
import de.intarsys.security.smartcard.card.EnumCardState;

/**
 * The abstraction of a smartcard inserted in a {@link StandardCardTerminal}. A
 * {@link StandardCard} can be achieved by calling {@link
 * CardTerminal.getCard()}
 * <p>
 * {@link StandardCard} is part of the abstraction layer that is built on top of
 * the PCSC API.
 * 
 */
public class StandardCard extends CommonCard {

	private final static Logger Log = PACKAGE.Log;

	public StandardCard(StandardCardTerminal cardTerminal, ATR atr) {
		super(cardTerminal, atr);
	}

	@Override
	protected CommonCardConnection basicConnectExclusive(int id, int protocol,
			ScheduledExecutorService executor) throws CardException {
		return basicGetCardTerminal().basicConnectExclusive(this, id, protocol,
				executor);
	}

	@Override
	protected CommonCardConnection basicConnectShared(int id, int protocol,
			ScheduledExecutorService executor)
			throws CardSharingViolationException, CardException {
		return basicGetCardTerminal().basicConnectShared(this, id, protocol,
				executor);
	}

	@Override
	protected StandardCardTerminal basicGetCardTerminal() {
		return (StandardCardTerminal) super.basicGetCardTerminal();
	}

	/*
	 * make method available in package
	 * 
	 * @see de.intarsys.security.smartcard.card.CommonCard#dispose()
	 */
	@Override
	protected void dispose() {
		super.dispose();
	}

	/*
	 * make method available in package
	 * 
	 * @see
	 * de.intarsys.security.smartcard.card.CommonCard#setState(de.intarsys.security
	 * .smartcard.card.EnumCardState)
	 */
	@Override
	protected void setState(EnumCardState newState) {
		super.setState(newState);
	}

}
