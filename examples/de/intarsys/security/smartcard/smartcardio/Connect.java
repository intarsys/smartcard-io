package de.intarsys.security.smartcard.smartcardio;

import java.security.Security;
import java.util.List;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.CardTerminals.State;
import javax.smartcardio.TerminalFactory;

public class Connect {

	public static void main(String[] args) {
		try {
			new Connect().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void connect(CardTerminal cardTerminal) throws Exception {
		System.out.println("" + cardTerminal + " connect");
		Card card = cardTerminal.connect("*");
		try {
			System.out.println("" + card + " begin transaction");
			card.beginExclusive();
			System.out.println("" + card + " end transaction");
			card.endExclusive();
		} finally {
			System.out.println("" + card + " disconnect");
			card.disconnect(false);
		}
	}

	public void run() throws Exception {
		Security.insertProviderAt(new SmartcardioProvider(), 1);
		//
		TerminalFactory factory = TerminalFactory.getDefault();
		CardTerminals terminals = factory.terminals();
		while (System.in.available() == 0) {
			List<CardTerminal> list = terminals.list(State.CARD_INSERTION);
			if (list.isEmpty()) {
				System.out.println("no terminals");
			}
			for (CardTerminal cardTerminal : list) {
				try {
					connect(cardTerminal);
				} catch (Exception e) {
					System.out.println("error connecting " + e);
				}
			}
			System.out.println("wait for change");
			if (terminals.waitForChange(1000)) {
				System.out.println("change detected...");
			} else {
				System.out.println("timeout");
			}
		}
	}
}
