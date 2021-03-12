package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines;

import com.sirius.sdk.base.AbstractStateMachine;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProtocolMessage;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.pairwise.Pairwise;

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
