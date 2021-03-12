package com.sirius.sdk.base;

import com.sirius.sdk.hub.Context;

import java.util.List;

public abstract class AbstractStateMachine {
    protected Context context;
    protected int timeToLiveSec = 60;

    public abstract List<String> protocols();
}
