package com.sirius.sdk.hub.coprotocols;

import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.hub.Hub;

public class AbstractCoProtocol {

    int timeToLiveSec = 30;
    boolean isAborted = false;
    Hub hub = null;
    boolean started = false;
    AbstractCoProtocolTransport transport = null;

    public AbstractCoProtocol() {

    }

    public AbstractCoProtocol(int timeToLiveSec) {
        this.timeToLiveSec = timeToLiveSec;
    }
}
