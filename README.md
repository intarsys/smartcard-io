# intarsys smartcard-io

## Overview

This project contains a PC/SC wrapper and smartcard abstraction layer. 

## Build

The project is provided as a self-contained Gradle project and should compile without problems.

## Usage

A pre-built jar file is available in the "dist" folder.

### PC/SC native usage

On the most basic layer, we simply use a declarative approach to wrap PC/SC API functions. To work on this "abstraction" you can simply use *de.intarsys.security.smartcard.pcsc.nativec._PCSC*.

If you create this without parameter, the default library for the platform will be used.

### PC/SC abstraction usage

There's a small abstract layer around *de.intarsys.security.smartcard.pcsc.nativec._PCSC* that allows for a little bit more readable code and also includes some indirections to ease life with 3rd-party libraries and workarounds for them.

In the "examples" directory you will find some demo code.

The most simple use case is 

```
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
```

To  connect to a reader 

```
// recommended use: create a new context for the connection
System.out.println("" + reader + " establish context");
IPCSCContext connectionContext = reader.getContext().establishContext();
System.out.println("" + reader + " connect");
IPCSCConnection connection = connectionContext.connect("example", reader.getName(), _IPCSC.SCARD_SHARE_SHARED,
        _IPCSC.SCARD_PROTOCOL_Tx);
System.out.println("" + reader + " begin transaction");
connection.beginTransaction();
System.out.println("" + reader + " end transaction");
connection.endTransaction(_IPCSC.SCARD_LEAVE_CARD);
System.out.println("" + reader + " disconnect");
connection.disconnect(_IPCSC.SCARD_LEAVE_CARD);
System.out.println("" + reader + " dispose context");
connectionContext.dispose();
```

## License

```
BSD 3-Clause License

Copyright (c) 2013, intarsys GmbH

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```

## Service & Support

Service & support should be funneled through the tools available with GitHub.
