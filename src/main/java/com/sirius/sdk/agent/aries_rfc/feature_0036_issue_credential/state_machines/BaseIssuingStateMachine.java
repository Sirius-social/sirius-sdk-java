package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines;

import com.sirius.sdk.agent.aries_rfc.AriesProblemReport;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.IssueProblemReport;
import com.sirius.sdk.base.AbstractStateMachine;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.BaseIssueCredentialMessage;

import java.util.Arrays;
import java.util.List;

public abstract class BaseIssuingStateMachine extends AbstractStateMachine {
    public static final String PROPOSE_NOT_ACCEPTED = "propose_not_accepted";
    public static final String OFFER_PROCESSING_ERROR = "offer_processing_error";
    public static final String REQUEST_NOT_ACCEPTED = "request_not_accepted";
    public static final String ISSUE_PROCESSING_ERROR = "issue_processing_error";
    public static final String RESPONSE_FOR_UNKNOWN_REQUEST = "response_for_unknown_request";

    IssueProblemReport problemReport = null;

    @Override
    public List<String> protocols() {
        return Arrays.asList(BaseIssueCredentialMessage.PROTOCOL, Ack.PROTOCOL);
    }
}
