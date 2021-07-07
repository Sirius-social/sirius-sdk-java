package com.sirius.sdk.agent.coprotocols;

import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;

import java.util.List;

public class TheirEndpointCoProtocolTransport extends AbstractCloudCoProtocolTransport {
    String myVerkey;
    TheirEndpoint endpoint;


    public TheirEndpointCoProtocolTransport(String myVerkey, TheirEndpoint endpoint,AgentRPC rpc) {
        super(rpc);
        this.myVerkey = myVerkey;
        this.endpoint = endpoint;
        setup(endpoint.getVerkey(), endpoint.getEndpointAddress(), myVerkey, endpoint.getRoutingKeys());
    }

    @Override
    public void start(List<String> protocols, int timeToLiveSec) {
        super.start(protocols, timeToLiveSec);
        this.rpc.startProtocolForP2P(myVerkey, endpoint.getVerkey(), protocols, timeToLiveSec);
    }

    @Override
    public void start(List<String> protocols) {
        super.start(protocols);
        this.rpc.startProtocolForP2P(myVerkey, endpoint.getVerkey(), protocols, timeToLiveSec);
    }

    @Override
    public void start(int timeToLiveSec) {
        super.start(timeToLiveSec);
        this.rpc.startProtocolForP2P(myVerkey, endpoint.getVerkey(), protocols, timeToLiveSec);
    }

    @Override
    public void start() {
        super.start(protocols);
        this.rpc.startProtocolForP2P(myVerkey, endpoint.getVerkey(), protocols, timeToLiveSec);
    }

    @Override
    public void stop() {
        super.stop();
        this.rpc.stopProtocolForP2P(myVerkey, endpoint.getVerkey(), protocols, true);
    }
}
