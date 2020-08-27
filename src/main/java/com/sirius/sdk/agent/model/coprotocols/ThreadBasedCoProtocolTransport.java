package com.sirius.sdk.agent.model.coprotocols;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.model.pairwise.Pairwise;

import java.util.ArrayList;
import java.util.List;

/**
 *  CoProtocol based on ~thread decorator
 *
 *  See details:
 *   - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0008-message-id-and-threading
 *
 */
public class ThreadBasedCoProtocolTransport extends AbstractCoProtocolTransport{

    String thid;
    Pairwise pairwise;
    String pthid;
    List<String> receivedOrders;
    int senderOrder;


    public ThreadBasedCoProtocolTransport(String thid, Pairwise pairwise, AgentRPC rpc,String pthid) {
        super(rpc);
        this.thid = thid;
        this.pairwise = pairwise;
        this.pthid = pthid;
        senderOrder = 0;
        receivedOrders = new ArrayList<>();

    }

}
