package com.sirius.sdk.agent.consensus.simple;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;

public class SimpleConsensusMessage extends AriesProtocolMessage {
    @Override
    public String getProtocol() {
        return "simple-consensus";
    }

    @Override
    public String getName() {
        return null;
    }
}
