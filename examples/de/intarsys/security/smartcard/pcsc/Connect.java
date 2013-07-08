package de.intarsys.security.smartcard.pcsc;

import java.util.List;

import de.intarsys.security.smartcard.pcsc.PCSCStatusMonitor.IStatusListener;
import de.intarsys.security.smartcard.pcsc.nativec._IPCSC;

public class Connect {

	public static void main(String[] args) {
		try {
			new Connect().run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void connect(IPCSCCardReader reader) throws PCSCException {
		// recommended use: create a new context for the connection
		System.out.println("" + reader + " establish context");
		IPCSCContext connectionContext = reader.getContext().establishContext();
		System.out.println("" + reader + " connect");
		IPCSCConnection connection = connectionContext.connect(
				reader.getName(), _IPCSC.SCARD_SHARE_SHARED,
				_IPCSC.SCARD_PROTOCOL_Tx);
		System.out.println("" + reader + " begin transaction");
		connection.beginTransaction();
		System.out.println("" + reader + " end transaction");
		connection.endTransaction(_IPCSC.SCARD_LEAVE_CARD);
		System.out.println("" + reader + " disconnect");
		connection.disconnect(_IPCSC.SCARD_LEAVE_CARD);
		System.out.println("" + reader + " dispose context");
		connectionContext.dispose();
	}

	public void run() throws Exception {
		IPCSCContext context = PCSCContextFactory.get().establishContext();
		List<IPCSCCardReader> readers = context.listReaders();
		if (readers.isEmpty()) {
			System.out.println("no reader found");
			return;
		}
		for (IPCSCCardReader reader : readers) {
			final PCSCStatusMonitor monitor = new PCSCStatusMonitor(reader);
			monitor.addStatusListener(new IStatusListener() {
				@Override
				public void onException(IPCSCCardReader reader, PCSCException e) {
					e.printStackTrace();
				}

				@Override
				public void onStatusChange(IPCSCCardReader reader,
						PCSCCardReaderState cardReaderState) {
					System.out.println("reader " + cardReaderState.getReader()
							+ " state " + cardReaderState);
					if (cardReaderState.isPresent()) {
						try {
							monitor.stop();
							connect(reader);
						} catch (PCSCException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		while (System.in.read() != '\n') {
			Thread.sleep(100);
		}
		context.dispose();
	}
}
