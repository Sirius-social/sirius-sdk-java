package com.sirius.sdk.agent.model.coprotocols;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.model.pairwise.TheirEndpoint;

import java.util.List;

public class TheirEndpointCoProtocolTransport extends AbstractCoProtocolTransport {
    String myVerkey;
    TheirEndpoint endpoint;


    public TheirEndpointCoProtocolTransport(String myVerkey, TheirEndpoint endpoint,AgentRPC rpc) {
        super(rpc);
        this.myVerkey = myVerkey;
        this.endpoint = endpoint;
        setup(endpoint.getVerkey(), endpoint.getEndpoint(), myVerkey, endpoint.getRoutingKeys());
    }

    @Override
    public void start(List<String> protocols, int timeToLiveSec) {
        super.start(protocols, timeToLiveSec);
        this.rpc.startProtocolForP2P(myVerkey, endpoint.getVerkey(), protocols, timeToLiveSec);
    }

    @Override
    public void stop() {
        super.stop();
        this.rpc.stopProtocolForP2P(myVerkey, endpoint.getVerkey(), protocols, true);
    }
}
