package de.intarsys.security.smartcard.smartcardio;

import java.security.Security;
import java.util.List;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

public class ListReaders {

	public static void main(String[] args) {
		try {
			new ListReaders().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() throws Exception {
		Security.insertProviderAt(new SmartcardioProvider(), 1);
		//

		// use "getInstance" to provide more detailed access
		// TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);

		TerminalFactory factory = TerminalFactory.getDefault();
		CardTerminals terminals = factory.terminals();
		List<CardTerminal> list = terminals.list();
		for (CardTerminal terminal : list) {
			System.out.println("found " + terminal + " named "
					+ terminal.getName());
		}

	}
}
