package com.sirius.sdk.hub.coprotocols;

import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.Hub;

import java.io.Closeable;

public abstract class AbstractCoProtocol implements Closeable {

    int timeToLiveSec = 60;
    boolean isAborted = false;
    Hub hub = null;
    boolean started = false;
    AbstractCoProtocolTransport transport = null;
    Context context;

    protected AbstractCoProtocol(Context context) {
        this.context = context;
    }

}
