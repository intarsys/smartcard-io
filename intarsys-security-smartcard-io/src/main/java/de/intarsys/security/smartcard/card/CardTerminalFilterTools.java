package de.intarsys.security.smartcard.card;

import de.intarsys.tools.reflect.ObjectCreationException;

public class CardTerminalFilterTools {

	public static ICardTerminalFilter createCardTerminalFilter(Object value) throws ObjectCreationException {
		if (value == null) {
			return null;
		}
		throw new ObjectCreationException();
	}

}
