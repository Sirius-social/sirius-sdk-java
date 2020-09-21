package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.AbstractStateMachine;
import com.sirius.sdk.agent.TransportLayer;

import java.util.List;
import java.util.logging.Logger;

public class StateMachineIssue  extends AbstractStateMachine {
    public StateMachineIssue(TransportLayer transportLayer, int timeToLive, Logger logger) {
        super(transportLayer, timeToLive, logger);
    }

    @Override
    public List<String> protocols() {
        return null;
    }
}
