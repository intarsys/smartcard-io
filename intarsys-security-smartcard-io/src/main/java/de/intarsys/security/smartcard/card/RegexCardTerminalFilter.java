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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexCardTerminalFilter implements ICardTerminalFilter {

	private static final String PATTERN_STRING = "\\s*(\\w*)\\s*[:=]([^;]*);?"; //$NON-NLS-1$

	private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

	public static RegexCardTerminalFilter create(String definition) {
		RegexCardTerminalFilter filter = new RegexCardTerminalFilter();
		Matcher m = PATTERN.matcher(definition);
		while (m.find()) {
			String key = m.group(1);
			String value = m.group(2);
			filter.addMatch(key, value.trim());
		}
		return filter;
	}

	private Map<String, Pattern> patterns = new HashMap<String, Pattern>();

	@Override
	public boolean accept(ICardTerminal terminal) {
		Pattern pattern;
		//
		pattern = patterns.get("name"); //$NON-NLS-1$
		if (pattern != null && !pattern.matcher(terminal.getName()).matches()) {
			return false;
		}
		return true;
	}

	public void addMatch(String key, String regularExpression) {
		patterns.put(key, Pattern.compile(regularExpression));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
			sb.append(entry.getKey());
			sb.append("="); //$NON-NLS-1$
			sb.append(entry.getValue().pattern());
		}
		return sb.toString();
	}
}
