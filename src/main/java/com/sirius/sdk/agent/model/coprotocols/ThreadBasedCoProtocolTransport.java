package com.sirius.sdk.agent.model.coprotocols;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
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
    JSONObject receivedOrders;
    int senderOrder = 0;
    Pairwise.Their their = null;

    public ThreadBasedCoProtocolTransport(String thid, Pairwise pairwise, AgentRPC rpc,String pthid) {
        super(rpc);
        this.thid = thid;
        this.pairwise = pairwise;
        this.their = pairwise.getTheir();
        this.pthid = pthid;
        senderOrder = 0;
        receivedOrders = new JSONObject();
        setup(pairwise.getTheir().getVerkey(), pairwise.getTheir().getEndpoint(), pairwise.getMe().getVerkey(), pairwise.getTheir().getRoutingKeys());
    }

    @Override
    public Pair<Boolean, Message> wait(Message message) throws SiriusPendingOperation, SiriusInvalidPayloadStructure, SiriusInvalidMessage {
        prepareMessage(message);
        Pair<Boolean, Message> res = super.wait(message);
        Message response = res.second;
        if (res.first) {
            if (response.messageObjectHasKey(THREAD_DECORATOR)) {
                if (response.getMessageObj().getJSONObject(THREAD_DECORATOR).has("sender_order")) {
                    int respondSenderOrder = response.getMessageObj().getJSONObject(THREAD_DECORATOR).getInt("sender_order");
                    if (this.their != null) {
                        String recipient = this.their.getDid();
                        //err = DIDField().validate(recipient)
                        //if err is None:
                        {
                            int order = receivedOrders.optInt(recipient, 0);
                            receivedOrders.put(recipient, Math.max(order, respondSenderOrder));
                        }
                    }
                }
            }
        }
        return res;
    }

    private void prepareMessage(Message msg) {
        if (!msg.messageObjectHasKey(THREAD_DECORATOR)) {
            JSONObject threadDecorator = new JSONObject().
                    put("thid", this.thid).
                    put("sender_order", senderOrder);
            if (pthid != null && !pthid.isEmpty()) {
                threadDecorator.put("pthid", pthid);
            }

            if (receivedOrders != null) {
                threadDecorator.put("received_orders", receivedOrders);
            }

            this.senderOrder++;
            msg.getMessageObj().put(THREAD_DECORATOR, threadDecorator);
        }
    }

    @Override
    public void start(List<String> protocols, int timeToLiveSec) {
        super.start(protocols, timeToLiveSec);
        this.rpc.startProtocolWithThreading(thid, timeToLiveSec);
    }

    @Override
    public void start(List<String> protocols) {
        super.start(protocols);
        this.rpc.startProtocolWithThreading(thid, timeToLiveSec);
    }

    @Override
    public void start() {
        super.start(protocols);
        this.rpc.startProtocolWithThreading(thid, timeToLiveSec);
    }

    @Override
    public void stop() {
        super.stop();
        this.rpc.stopProtocolWithThreading(thid, true);
    }
}
