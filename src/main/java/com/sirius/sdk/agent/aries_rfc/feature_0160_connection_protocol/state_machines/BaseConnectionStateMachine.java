package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines;

import com.sirius.sdk.agent.AbstractStateMachine;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProtocolMessage;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.model.pairwise.TheirEndpoint;

import java.util.Arrays;
import java.util.List;

public abstract class BaseConnectionStateMachine extends AbstractStateMachine {
    Pairwise.Me me = null;
    Endpoint myEndpoint = null;

    @Override
    public List<String> protocols() {
        return Arrays.asList(ConnProtocolMessage.PROTOCOL, Ack.PROTOCOL, Ping.PROTOCOL);
    }
}
