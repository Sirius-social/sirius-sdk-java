package com.sirius.sdk.hub.coprotocols;

import com.sirius.sdk.agent.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.hub.Context;

import java.io.Closeable;

public abstract class AbstractCoProtocol implements Closeable {

    int timeToLiveSec = 60;
    boolean isAborted = false;
    boolean started = false;
    AbstractCoProtocolTransport transport = null;
    Context context;

    protected AbstractCoProtocol(Context context) {
        this.context = context;
    }

    @Override
    public void close() {
        if (started) {
            transport.stop();
            started = false;
            transport = null;
        }
    }
}
