package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines;

import com.sirius.sdk.agent.aries_rfc.AriesProblemReport;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.IssueProblemReport;
import com.sirius.sdk.base.AbstractStateMachine;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.BaseIssueCredentialMessage;

import java.util.Arrays;
import java.util.List;

public abstract class BaseIssuingStateMachine extends AbstractStateMachine {

    IssueProblemReport problemReport = null;

    @Override
    public List<String> protocols() {
        return Arrays.asList(BaseIssueCredentialMessage.PROTOCOL, Ack.PROTOCOL);
    }
}
