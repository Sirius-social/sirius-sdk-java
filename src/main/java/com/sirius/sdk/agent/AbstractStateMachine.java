package com.sirius.sdk.agent;

import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractStateMachine {
    protected AbstractCoProtocolTransport coprotocol = null;
    protected Context context;
    int timeToLiveSec = 60;

    protected void createCoprotocol(Pairwise pairwise) {
        if (coprotocol == null) {
            coprotocol = context.agent.spawn(pairwise);
            coprotocol.start(protocols());
        }
    }

    protected void releaseCoprotocol() {
        if (coprotocol != null) {
            coprotocol.stop();
            coprotocol = null;
        }
    }

    public abstract List<String> protocols();
}
