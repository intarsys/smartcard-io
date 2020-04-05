package de.intarsys.security.smartcard.pcsc;

import java.util.List;

import de.intarsys.security.smartcard.pcsc.IPCSCCardReader;
import de.intarsys.security.smartcard.pcsc.IPCSCContext;
import de.intarsys.security.smartcard.pcsc.PCSCContextFactory;

public class ListReaders {

	public static void main(String[] args) {
		try {
			new ListReaders().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() throws Exception {
		IPCSCContext context = PCSCContextFactory.get().establishContext();
		List<IPCSCCardReader> readers = context.listReaders();
		for (IPCSCCardReader reader : readers) {
			System.out
					.println("found " + reader + " named " + reader.getName());
		}
		context.dispose();
	}
}
