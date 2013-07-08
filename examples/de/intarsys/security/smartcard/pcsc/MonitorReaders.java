package de.intarsys.security.smartcard.pcsc;

import java.util.List;

import de.intarsys.security.smartcard.pcsc.PCSCStatusMonitor.IStatusListener;

public class MonitorReaders {

	public static void main(String[] args) {
		try {
			new MonitorReaders().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() throws Exception {
		IPCSCContext context = PCSCContextFactory.get().establishContext();
		List<IPCSCCardReader> readers = context.listReaders();
		if (readers.isEmpty()) {
			System.out.println("no reader found");
			return;
		}
		for (IPCSCCardReader reader : readers) {
			PCSCStatusMonitor monitor = new PCSCStatusMonitor(reader);
			monitor.addStatusListener(new IStatusListener() {
				@Override
				public void onException(IPCSCCardReader reader, PCSCException e) {
					e.printStackTrace();
				}

				@Override
				public void onStatusChange(IPCSCCardReader reader,
						PCSCCardReaderState cardReaderState) {
					System.out.println("reader " + reader + " state "
							+ cardReaderState);
				}
			});
		}
		while (System.in.read() != '\n') {
			Thread.sleep(100);
		}
		context.dispose();
	}
}
