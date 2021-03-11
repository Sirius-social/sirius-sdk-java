package com.sirius.sdk.agent;

import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractStateMachine {
    protected Context context;
    protected int timeToLiveSec = 60;

    public abstract List<String> protocols();
}
