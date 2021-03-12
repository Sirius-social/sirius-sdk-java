package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines;

import com.sirius.sdk.base.AbstractStateMachine;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.BasePresentProofMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.PresentProofProblemReport;

import java.util.Arrays;
import java.util.List;

public abstract class BaseVerifyStateMachine extends AbstractStateMachine {

    PresentProofProblemReport problemReport = null;

    public PresentProofProblemReport getProblemReport() {
        return problemReport;
    }

    @Override
    public List<String> protocols() {
        return Arrays.asList(BasePresentProofMessage.PROTOCOL, Ack.PROTOCOL);
    }
}
