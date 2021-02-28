package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof;

import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;

import java.util.Arrays;

public class BaseVerifyStateMachine {
    AbstractCoProtocolTransport coprotocol = null;
    Context context;
    PresentProofProblemReport problemReport = null;
    int timeToLiveSec = 60;

    protected void createCoprotocol(Pairwise pairwise) {
        if (coprotocol == null) {
            coprotocol = context.agent.spawn(pairwise);
            coprotocol.start(Arrays.asList(BasePresentProofMessage.PROTOCOL, Ack.PROTOCOL));
        }
    }

    protected void releaseCoprotocol() {
        if (coprotocol != null) {
            coprotocol.stop();
            coprotocol = null;
        }
    }

    public PresentProofProblemReport getProblemReport() {
        return problemReport;
    }
}
