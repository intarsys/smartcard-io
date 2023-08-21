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
package de.intarsys.security.smartcard.card.standard;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.intarsys.security.smartcard.card.CardException;
import de.intarsys.security.smartcard.card.CommonCardSystem;
import de.intarsys.security.smartcard.card.ICardTerminal;
import de.intarsys.security.smartcard.pcsc.EmptyContext;
import de.intarsys.security.smartcard.pcsc.IPCSCCardReader;
import de.intarsys.security.smartcard.pcsc.IPCSCContext;
import de.intarsys.security.smartcard.pcsc.IPCSCContextFactory;
import de.intarsys.security.smartcard.pcsc.PCSCContextFactory;
import de.intarsys.security.smartcard.pcsc.PCSCException;
import de.intarsys.security.smartcard.pcsc.nativec._PCSC_RETURN_CODES;
import de.intarsys.tools.event.AttributeChangedEvent;
import de.intarsys.tools.exception.ExceptionTools;
import de.intarsys.tools.yalf.api.ILogger;

/**
 * The abstraction of the smartcard environment connected to the system. The
 * {@link StandardCardSystem} is connected to an {@link IPCSCContext}, that
 * defines access to the lower level PCSC abstraction.
 * <p>
 * {@link StandardCardSystem} monitors the PCSC environment in a monitoring
 * thread. All available {@link ICardTerminal} tokens are listed. Token events
 * (found and removed) are propagated to registered listeners as
 * {@link AttributeChangedEvent} events.
 * <p>
 * {@link StandardCardSystem} is used in a multithreaded environment and its
 * state is accessed by either the monitoring thread and the client code
 * requesting the {@link StandardCardTerminal} instances.
 */
public class StandardCardSystem extends CommonCardSystem {

	public static final StandardCardSystem DISABLED = new StandardCardSystem(new EmptyContext());

	private static final ILogger Log = PACKAGE.Log;

	private Pattern cardTerminalsHide = Pattern.compile(".*datev.*", Pattern.CASE_INSENSITIVE);

	private IPCSCContext pcscContext;

	private final IPCSCContextFactory pcscContextFactory;

	public StandardCardSystem(IPCSCContext context) {
		super();
		this.pcscContext = context;
		this.pcscContextFactory = null;
	}

	public StandardCardSystem(IPCSCContextFactory factory) {
		super();
		if (factory == null) {
			this.pcscContextFactory = PCSCContextFactory.get();
		} else {
			this.pcscContextFactory = factory;
		}
	}

	@Override
	protected void basicDispose() {
		pcscStop();
	}

	protected IPCSCContext getPcscContext() {
		return pcscContext;
	}

	private void pcscRestart() {
		pcscStop();
		pcscStart();
	}

	private void pcscStart() {
		if (pcscContext == null) {
			try {
				pcscContext = pcscContextFactory.establishContext();
			} catch (PCSCException e) {
				if (e.getErrorCode() == _PCSC_RETURN_CODES.SCARD_E_SERVICE_STOPPED) {
					/*
					 * last card reader removed, windows may shut down service
					 * and restart upon card reader connect - retry later
					 */
				} else {
					Log.warn("{} can't establish PCSC context", this);
					pcscContext = new EmptyContext();
				}
			} catch (Exception e) {
				Log.warn("{} can't establish PCSC context", this, e);
				pcscContext = new EmptyContext();
			}
		}
	}

	private void pcscStop() {
		if (pcscContext != null) {
			try {
				pcscContext.dispose();
				pcscContext = null;
			} catch (PCSCException e) {
				// ignore
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (isEnabled()) {
			if (getPcscContext() instanceof EmptyContext) {
				pcscStop();
			}
			pcscStart();
		} else {
			pcscStop();
			pcscContext = new EmptyContext();
		}
	}

	@Override
	public String toString() {
		return "StandardCardSystem"; //$NON-NLS-1$
	}

	@Override
	protected void updateCardTerminals(Map<String, ICardTerminal> oldTerminals,
			Map<String, ICardTerminal> newTerminals) {
		pcscStart();
		// kill all terminals in case of pcsc failure
		List<IPCSCCardReader> pcscReaders = Collections.EMPTY_LIST;
		if (getPcscContext() == null) {
			return;
		}
		try {
			pcscReaders = getPcscContext().listReaders();
		} catch (PCSCException e) {
			if (e.getErrorCode() == _PCSC_RETURN_CODES.ERROR_INVALID_HANDLE) {
				Log.warn("{} PC/SC context invalid. Restart card system", this, e); //$NON-NLS-1$
				pcscRestart();
			} else if (e.getErrorCode() == _PCSC_RETURN_CODES.ERROR_RPC_FAILED) {
				Log.warn("{} PC/SC context rpc failed. Restart card system", this, e); //$NON-NLS-1$
				pcscRestart();
			} else if (e.getErrorCode() == _PCSC_RETURN_CODES.SCARD_E_SERVICE_STOPPED) {
				/*
				 * last card reader removed, windows may shut down service
				 */
				Log.warn("{} PC/SC context service stopped. Restart card system", this); //$NON-NLS-1$
				pcscRestart();
			} else {
				Log.debug("{} PC/SC list readers failed ({}) - call ignored", this, ExceptionTools.getMessage(e)); //$NON-NLS-1$
			}
		}
		for (IPCSCCardReader pcscReader : pcscReaders) {
			if (cardTerminalsHide.matcher(pcscReader.getName()).matches()) {
				continue;
			}
			ICardTerminal terminal = oldTerminals.remove(pcscReader.getName());
			if (terminal == null || terminal.isDisposed()) {
				// found a new terminal
				try {
					StandardCardTerminal tempTerminal = new StandardCardTerminal(this, pcscReader);
					newTerminals.put(tempTerminal.getName(), tempTerminal);
				} catch (CardException e) {
					if (e.getCause() instanceof PCSCException) {
						if (((PCSCException) e.getCause())
								.getErrorCode() == _PCSC_RETURN_CODES.SCARD_E_UNKNOWN_READER) {
							// we had the case where listReaders returned invalid handles????
							Log.debug("{} PC/SC unknown reader {} detected", this, pcscReader, e); //$NON-NLS-1$
							continue;
						}
					}
					Log.warn("{} PC/SC creating {} failed", this, pcscReader, e); //$NON-NLS-1$
				} catch (Exception e) {
					Log.warn("{} PC/SC creating {} failed", this, pcscReader, e); //$NON-NLS-1$
				}
			}
		}
	}
}
