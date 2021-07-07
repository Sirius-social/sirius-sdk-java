package com.sirius.sdk.agent;

import com.sirius.sdk.agent.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.PairwiseCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.TheirEndpointCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;

public abstract class TransportLayer {

    public abstract AbstractCoProtocolTransport spawn(String my_verkey, TheirEndpoint endpoint);

    public abstract AbstractCoProtocolTransport spawn(Pairwise pairwise );

    public abstract AbstractCoProtocolTransport spawn(String thid,Pairwise pairwise);

    public abstract AbstractCoProtocolTransport spawn(String thid);

    public abstract AbstractCoProtocolTransport spawn(String thid, Pairwise pairwise , String pthid);

    public abstract AbstractCoProtocolTransport spawn(String thid,  String pthid);

}
