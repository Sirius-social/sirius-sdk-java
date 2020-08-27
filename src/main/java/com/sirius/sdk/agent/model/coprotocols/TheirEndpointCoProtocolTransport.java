package com.sirius.sdk.agent.model.coprotocols;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.model.pairwise.TheirEndpoint;

public class TheirEndpointCoProtocolTransport extends AbstractCoProtocolTransport {
    String myVerkey;
    TheirEndpoint endpoint;

    public TheirEndpointCoProtocolTransport(String myVerkey, TheirEndpoint endpoint,AgentRPC rpc) {
        super(rpc);
        this.myVerkey = myVerkey;
        this.endpoint = endpoint;
        setup(endpoint.getVerkey(), endpoint.getEndpoint(), myVerkey, endpoint.getRoutingKeys());
    }
}
