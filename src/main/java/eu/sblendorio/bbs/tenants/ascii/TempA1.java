package eu.sblendorio.bbs.tenants.ascii;

import eu.sblendorio.bbs.core.AsciiThread;

public class TempA1 extends AsciiThread {

    @Override
    public void doLoop() throws Exception {
        println("Hello world!");
        resetInput();
        readKey();
        println("Consciously closing the connection.");
    }
}
