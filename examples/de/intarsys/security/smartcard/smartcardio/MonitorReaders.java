package de.intarsys.security.smartcard.smartcardio;

import java.security.Security;
import java.util.List;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import de.intarsys.security.smartcard.smartcardio.CardTerminalMonitor.ICardTerminalListener;

public class MonitorReaders {

	public static void main(String[] args) {
		try {
			new MonitorReaders().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() throws Exception {
		Security.insertProviderAt(new SmartcardioProvider(), 1);
		//
		TerminalFactory factory = TerminalFactory.getDefault();
		CardTerminals terminals = factory.terminals();
		List<CardTerminal> list = terminals.list();
		for (CardTerminal terminal : list) {
			CardTerminalMonitor monitor = new CardTerminalMonitor(terminal);
			monitor.addStatusListener(new ICardTerminalListener() {

				@Override
				public void onException(CardTerminal terminal, CardException e) {
					e.printStackTrace();
				}

				@Override
				public void onStatusChange(CardTerminal terminal,
						boolean present) {
					System.out.println("reader " + terminal + " present "
							+ present);
				}
			});
		}
		while (System.in.available() == 0) {
			Thread.sleep(100);
		}
	}
}
