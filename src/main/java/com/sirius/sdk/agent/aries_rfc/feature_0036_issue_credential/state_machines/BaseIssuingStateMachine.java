package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines;

import com.sirius.sdk.agent.AbstractStateMachine;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.BaseIssueCredentialMessage;
import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;

import java.util.Arrays;
import java.util.List;

public abstract class BaseIssuingStateMachine extends AbstractStateMachine {

    @Override
    public List<String> protocols() {
        return Arrays.asList(BaseIssueCredentialMessage.PROTOCOL, Ack.PROTOCOL);
    }
}
