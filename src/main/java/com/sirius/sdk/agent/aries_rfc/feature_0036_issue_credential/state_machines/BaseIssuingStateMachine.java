package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines;

import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.BaseIssueCredentialMessage;
import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;

import java.util.Arrays;

public abstract class BaseIssuingStateMachine {
    AbstractCoProtocolTransport coprotocol = null;
    Context context;

    protected void createCoprotocol(Pairwise holder) {
        if (coprotocol == null) {
            coprotocol = context.agent.spawn(holder);
            coprotocol.start(Arrays.asList(BaseIssueCredentialMessage.PROTOCOL, Ack.PROTOCOL));
        }
    }

    protected void releaseCoprotocol() {
        if (coprotocol != null) {
            coprotocol.stop();
            coprotocol = null;
        }
    }
}
