package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines;

import com.sirius.sdk.base.AbstractStateMachine;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.BasePresentProofMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.PresentProofProblemReport;

import java.util.Arrays;
import java.util.List;

public abstract class BaseVerifyStateMachine extends AbstractStateMachine {
    public static final String PROPOSE_NOT_ACCEPTED = "propose_not_accepted";
    public static final String RESPONSE_NOT_ACCEPTED = "response_not_accepted";
    public static final String RESPONSE_PROCESSING_ERROR = "response_processing_error";
    public static final String REQUEST_NOT_ACCEPTED = "request_not_accepted";
    public static final String RESPONSE_FOR_UNKNOWN_REQUEST = "response_for_unknown_request";
    public static final String REQUEST_PROCESSING_ERROR = "request_processing_error";
    public static final String VERIFY_ERROR = "verify_error";

    PresentProofProblemReport problemReport = null;

    public PresentProofProblemReport getProblemReport() {
        return problemReport;
    }

    @Override
    public List<String> protocols() {
        return Arrays.asList(BasePresentProofMessage.PROTOCOL, Ack.PROTOCOL);
    }
}
