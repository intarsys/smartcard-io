package de.intarsys.security.smartcard.card;

import de.intarsys.tools.reflect.ObjectCreationException;

public class CardFilterTools {

	public static ICardFilter createCardFilter(Object value) throws ObjectCreationException {
		if (value == null) {
			return null;
		}
		throw new ObjectCreationException();
	}
}
