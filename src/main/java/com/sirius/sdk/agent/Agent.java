package com.sirius.sdk.agent;

import com.sirius.sdk.agent.model.coprotocols.PairwiseCoProtocolTransport;
import com.sirius.sdk.agent.model.coprotocols.TheirEndpointCoProtocolTransport;
import com.sirius.sdk.agent.model.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.model.pairwise.TheirEndpoint;

public class Agent extends TransportLayer{

    @Override
    public TheirEndpointCoProtocolTransport spawn(String myVerkey, TheirEndpoint endpoint) {
        return null;
    }

    @Override
    public PairwiseCoProtocolTransport spawn(Pairwise pairwise) {
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid, Pairwise pairwise) {
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid) {
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid, Pairwise pairwise, String pthid) {
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid, String pthid) {
        return null;
    }
}
