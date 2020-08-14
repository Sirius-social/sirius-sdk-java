package com.sirius.sdk.agent;

import com.sirius.sdk.agent.model.coprotocols.PairwiseCoProtocolTransport;
import com.sirius.sdk.agent.model.coprotocols.TheirEndpointCoProtocolTransport;
import com.sirius.sdk.agent.model.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.model.pairwise.TheirEndpoint;

public abstract  class TransportLayer {

    public abstract TheirEndpointCoProtocolTransport spawn(String my_verkey, TheirEndpoint endpoint);

    public abstract PairwiseCoProtocolTransport spawn(Pairwise pairwise );

    public abstract ThreadBasedCoProtocolTransport spawn(String thid,Pairwise pairwise);

    public abstract ThreadBasedCoProtocolTransport spawn(String thid);

    public abstract ThreadBasedCoProtocolTransport spawn(String thid, Pairwise pairwise , String pthid);

    public abstract ThreadBasedCoProtocolTransport spawn(String thid,  String pthid);

}
