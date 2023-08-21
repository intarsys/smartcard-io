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

import de.intarsys.tools.enumeration.EnumItem;
import de.intarsys.tools.enumeration.EnumMeta;

/**
 * The enumeration of supported states for a {@link ICard}.
 */
@SuppressWarnings("unchecked")
public class EnumCardState extends EnumItem {

	public static final EnumCardState CONNECTED_EXCLUSIVE = new EnumCardState(
			"connectedExclusive"); //$NON-NLS-1$

	public static final EnumCardState CONNECTED_SHARED = new EnumCardState(
			"connectedShared"); //$NON-NLS-1$

	public static final EnumCardState INVALID = new EnumCardState("invalid"); //$NON-NLS-1$

	/** The meta data for the enumeration. */
	public static final EnumMeta<EnumCardState> META = getMeta(EnumCardState.class);

	public static final EnumCardState NOT_CONNECTED = new EnumCardState(
			"notConnected"); //$NON-NLS-1$

	public static final EnumCardState UNKNOWN = new EnumCardState("unknown"); //$NON-NLS-1$

	static {
		UNKNOWN.setDefault();
	}

	protected EnumCardState(String id) {
		super(id);
	}

	public boolean isConnectedExclusive() {
		return this == CONNECTED_EXCLUSIVE;
	}

	public boolean isConnectedShared() {
		return this == CONNECTED_SHARED;
	}

	public boolean isInvalid() {
		return this == INVALID;
	}

	public boolean isNotConnected() {
		return this == NOT_CONNECTED || this == UNKNOWN;
	}

}
