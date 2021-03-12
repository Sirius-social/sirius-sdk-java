package com.sirius.sdk.agent.coprotocols;

import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.pairwise.Pairwise;

import java.util.List;

public class PairwiseCoProtocolTransport extends AbstractCoProtocolTransport {

    Pairwise pairwise;

    public PairwiseCoProtocolTransport(Pairwise pairwise, AgentRPC rpc) {
        super(rpc);
        this.pairwise = pairwise;
        setup(pairwise.getTheir().getVerkey(), pairwise.getTheir().getEndpoint(), pairwise.getMe().getVerkey(), pairwise.getTheir().getRoutingKeys());
    }

    @Override
    public void start(List<String> protocols, int timeToLiveSec) {
        super.start(protocols, timeToLiveSec);
        this.rpc.startProtocolForP2P(myVerkey, pairwise.getTheir().getVerkey(), protocols, timeToLiveSec);
    }

    @Override
    public void start(List<String> protocols) {
        super.start(protocols);
        this.rpc.startProtocolForP2P(myVerkey, pairwise.getTheir().getVerkey(), protocols, timeToLiveSec);
    }

    @Override
    public void start() {
        super.start(protocols);
        this.rpc.startProtocolForP2P(myVerkey, pairwise.getTheir().getVerkey(), protocols, timeToLiveSec);
    }

    @Override
    public void stop() {
        super.stop();
        this.rpc.stopProtocolForP2P(myVerkey, pairwise.getTheir().getVerkey(), protocols, true);
    }
}
