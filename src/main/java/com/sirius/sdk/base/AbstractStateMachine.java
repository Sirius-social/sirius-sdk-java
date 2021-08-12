package com.sirius.sdk.base;

import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractCoProtocol;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStateMachine {
    protected Context context;
    protected int timeToLiveSec = 60;
    protected List<AbstractCoProtocol> coprotocols = new ArrayList<>();

    public abstract List<String> protocols();

    public int getTimeToLiveSec() {
        return timeToLiveSec;
    }

    public void setTimeToLiveSec(int timeToLiveSec) {
        this.timeToLiveSec = timeToLiveSec;
    }
}
