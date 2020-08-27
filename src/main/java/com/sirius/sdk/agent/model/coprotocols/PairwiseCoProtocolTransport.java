package com.sirius.sdk.agent.model.coprotocols;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.model.pairwise.Pairwise;

public class PairwiseCoProtocolTransport extends AbstractCoProtocolTransport{

   /* def __init__(
            self, pairwise: Pairwise, rpc: AgentRPC
    ):
            super().__init__(rpc)
    self.__pairwise = pairwise
        self._setup(
    their_verkey=pairwise.their.verkey,
    endpoint=pairwise.their.endpoint,
    my_verkey=pairwise.me.verkey,
    routing_keys=pairwise.their.routing_keys
        )*/
    Pairwise pairwise;

    public PairwiseCoProtocolTransport(Pairwise pairwise, AgentRPC rpc) {
        super(rpc);
        this.pairwise = pairwise;

    }
}
