package com.sirius.sdk.agent;

import com.sirius.sdk.errors.BaseSiriusException;

public class StateMachineTerminatedWithError extends BaseSiriusException {

    String problemCode;
    String explain;
    boolean notify;

    public StateMachineTerminatedWithError(String problemCode, String explain, boolean notify) {
        super();
        this.problemCode = problemCode;
        this.explain = explain;
        this.notify = notify;
    }
    public StateMachineTerminatedWithError(String problemCode, String explain) {
        this(problemCode,explain,true);
    }

}
