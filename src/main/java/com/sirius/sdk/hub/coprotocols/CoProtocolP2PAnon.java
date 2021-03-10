package com.sirius.sdk.hub.coprotocols;

import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.model.pairwise.TheirEndpoint;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.List;

import static com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport.PLEASE_ACK_DECORATOR;
import static com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport.THREAD_DECORATOR;

public class CoProtocolP2PAnon extends AbstractP2PCoProtocol {
    String myVerkey;
    TheirEndpoint endpoint;
    List<String> protocols;
    String threadId;

    public CoProtocolP2PAnon(Context context, String myVerkey, TheirEndpoint endpoint, List<String> protocols, int timeToLiveSec) {
        super(context);
        this.myVerkey = myVerkey;
        this.endpoint = endpoint;
        this.protocols = protocols;
        this.timeToLiveSec = timeToLiveSec;
    }

    @Override
    public void send(Message message) throws SiriusPendingOperation {
        setup(message, false);
        getTransportLazy().send(message);
    }

    @Override
    public Pair<Boolean, Message> sendAndWait(Message message) throws SiriusInvalidPayloadStructure, SiriusInvalidMessage, SiriusPendingOperation {
        setup(message);
        Pair<Boolean, Message> res = getTransportLazy().sendAndWait(message);
        Message response = res.second;
        if (res.first) {
            if (response.messageObjectHasKey(PLEASE_ACK_DECORATOR)) {
                threadId = response.getMessageObj().getJSONObject(PLEASE_ACK_DECORATOR).optString("message_id");
                if (threadId.isEmpty())
                    threadId = message.getId();
            } else {
                threadId = "";
            }
        }
        return res;
    }

    private void setup(Message message, boolean pleaseAck) {
        if (pleaseAck) {
            if (!message.messageObjectHasKey(PLEASE_ACK_DECORATOR)) {
                message.getMessageObj().put(PLEASE_ACK_DECORATOR, new JSONObject().
                        put("message_id", message.getId()));
            }
        }

        if (!threadId.isEmpty()) {
            JSONObject thread = message.getMessageObj().optJSONObject(THREAD_DECORATOR);
            thread = thread != null ? thread : new JSONObject();
            if (!thread.has("thid")) {
                thread.put("thid", threadId);
                message.getMessageObj().put(THREAD_DECORATOR, thread);
            }
        }
    }

    private void setup(Message message) {
        setup(message, true);
    }

    private AbstractCoProtocolTransport getTransportLazy() {
        if (transport == null) {
            transport = context.getCurrentHub().getAgentConnectionLazy().spawn(myVerkey, endpoint);
            transport.start(protocols, timeToLiveSec);
            started = true;
        }
        return transport;
    }

    @Override
    public void close() {
        if (started) {
            transport.stop();
            started = false;
            transport = null;
        }
    }
}
