package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof;

import com.sirius.sdk.agent.AbstractStateMachine;
import com.sirius.sdk.agent.TransportLayer;

import java.util.List;
import java.util.logging.Logger;

public class StateMachineProver extends AbstractStateMachine {
    public StateMachineProver(TransportLayer transportLayer, int timeToLive, Logger logger) {
        super(transportLayer, timeToLive, logger);
    }

    @Override
    public List<String> protocols() {
        return null;
    }
}
