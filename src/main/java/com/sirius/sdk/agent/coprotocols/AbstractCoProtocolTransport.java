package com.sirius.sdk.agent.coprotocols;

import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.connections.RoutingBatch;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.messaging.Type;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Abstraction application-level protocols in the context of interactions among agent-like things.
 *
 *         Sirius SDK protocol is high-level abstraction over Sirius transport architecture.
 *         Approach advantages:
 *           - developer build smart-contract logic in block-style that is easy to maintain and control
 *           - human-friendly source code of state machines in procedural style
 *           - program that is running in separate coroutine: lightweight abstraction to start/kill/state-detection work thread
 *         See details:
 *           - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0003-protocols
 */
public abstract class AbstractCoProtocolTransport {

    public static final String THREAD_DECORATOR = "~thread";
    public static final String PLEASE_ACK_DECORATOR = "~please_ack";

    public AbstractCoProtocolTransport(AgentRPC rpc) {
        this.rpc = rpc;
    }

    AgentRPC rpc;
    String theirVK;
    String myVerkey;
    String endpoint;
    List<String> routingKeys;
    boolean isSetup;
    boolean started = false;
    List<String> protocols = new ArrayList<>();
    int timeToLiveSec = 30;
    Date dieTimestamp = null;
    List<String> pleaseAckIds = new ArrayList<>();
    boolean checkVerkeys = false;
    boolean checkProtocols = true;

    /**
     * Should be called in Descendant
     * @param theirVerkey
     * @param endpoint
     * @param myVerkey
     * @param routing_keys
     */
    public void setup(String theirVerkey, String endpoint, String myVerkey, List<String> routing_keys){
        this.theirVK = theirVerkey;
        this.myVerkey= myVerkey;
        this.endpoint = endpoint;
        this.routingKeys = routing_keys;
        if (routingKeys == null) {
            routingKeys = new ArrayList<>();
        }
        isSetup = true;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        this.dieTimestamp = null;
        this.protocols = new ArrayList<>();
        this.checkProtocols = false;
        started = true;
    }

    public void start(List<String> protocols) {
        this.dieTimestamp = null;
        this.protocols = protocols;
        if (protocols.isEmpty())
            this.checkProtocols = false;
        started = true;
    }

    public void start(int timeToLiveSec) {
        this.protocols = new ArrayList<>();
        this.checkProtocols = false;
        this.timeToLiveSec = timeToLiveSec;
        this.dieTimestamp = new Date(System.currentTimeMillis() + this.timeToLiveSec * 1000L);
        started = true;
    }

    public void start(List<String> protocols, int timeToLiveSec) {
        this.protocols = protocols;
        if (protocols.isEmpty())
            this.checkProtocols = false;
        this.timeToLiveSec = timeToLiveSec;
        this.dieTimestamp = new Date(System.currentTimeMillis() + this.timeToLiveSec * 1000L);
        started = true;
    }

    public void stop() {
        this.dieTimestamp = null;
        started = false;
        cleanupContext();
    }

    private void cleanupContext(Message message) {
        if (message.messageObjectHasKey(PLEASE_ACK_DECORATOR)) {
            String ackMessageId;
            if (message.getJSONOBJECTFromJSON(PLEASE_ACK_DECORATOR).has("message_id"))
                ackMessageId = message.getJSONOBJECTFromJSON(PLEASE_ACK_DECORATOR).getString("message_id");
            else
                ackMessageId = message.getId();
            rpc.stopProtocolWithThreads(pleaseAckIds, true);
            pleaseAckIds.removeIf(ackId -> ackId.equals(ackMessageId));
        }
    }

    private void cleanupContext() {
        this.rpc.stopProtocolWithThreads(pleaseAckIds, true);
        pleaseAckIds.clear();
    }

    private void setupContext(Message message) {
        if (message.messageObjectHasKey(PLEASE_ACK_DECORATOR)) {
            String ackMessageId;
            if (message.getJSONOBJECTFromJSON(PLEASE_ACK_DECORATOR).has("message_id"))
                ackMessageId = message.getJSONOBJECTFromJSON(PLEASE_ACK_DECORATOR).getString("message_id");
            else
                ackMessageId = message.getId();
            int ttl = this.timeToLiveSec;
            rpc.startProtocolWithThreads(Collections.singletonList(ackMessageId), ttl);
            pleaseAckIds.add(ackMessageId);
        }
    }

    public Pair<Boolean, Message> sendAndWait(Message message) throws SiriusPendingOperation, SiriusInvalidPayloadStructure, SiriusInvalidMessage {
        if (!isSetup) {
            throw new SiriusPendingOperation("You must Setup protocol instance at first");
        }

        rpc.setTimeout(timeToLiveSec);
        setupContext(message);

        Message event = null;
        try {
            event = rpc.sendMessage(message, Collections.singletonList(theirVK), endpoint, myVerkey, routingKeys, true);
        } catch (SiriusConnectionClosed siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        } catch (SiriusRPCError siriusRPCError) {
            siriusRPCError.printStackTrace();
        } finally {
            cleanupContext(message);
        }

        if (checkVerkeys) {
            String recipientVerkey = event.getStringFromJSON("recipient_verkey");
            String senderVerkey = event.getStringFromJSON("sender_verkey");
            if (!recipientVerkey.equals(myVerkey)) {
                throw new SiriusInvalidPayloadStructure("Unexpected recipient_verkey: " + recipientVerkey);
            }
            if (!senderVerkey.equals(theirVK)) {
                throw new SiriusInvalidPayloadStructure("Unexpected sender_verkey: " + senderVerkey);
            }
        }

        JSONObject payload = event.getJSONOBJECTFromJSON("message");
        if (payload != null) {
            Pair<Boolean, Message> okMsg = new Pair<>(false, null);
            try {
                okMsg = Message.restoreMessageInstance(payload.toString());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            if (!okMsg.first) {
                okMsg = new Pair<>(true, new Message(payload.toString()));
            }
            if (checkProtocols) {
                try {
                    if (!protocols.contains(Type.fromStr(message.getType()).getProtocol())) {
                        throw new SiriusInvalidMessage("@type has unexpected protocol " + Type.fromStr(message.getType()).getProtocol());
                    }
                } catch (SiriusInvalidType siriusInvalidType) {
                    siriusInvalidType.printStackTrace();
                }
            }

            return okMsg;
        } else {
            return new Pair<>(false, null);
        }
    }

    public static class GetOneResult {
        public Message message;
        public String senderVerkey;
        public  String recipientVerkey;

        public GetOneResult(Message message, String senderVerkey, String recipientVerkey) {
            this.message = message;
            this.senderVerkey = senderVerkey;
            this.recipientVerkey = recipientVerkey;
        }
    }

    public GetOneResult getOne() throws SiriusInvalidPayloadStructure {
        Message event = rpc.readProtocolMessage();
        Message message = null;
        if (event.messageObjectHasKey("message")) {
            try {
                Pair<Boolean, Message> okMessage = Message.restoreMessageInstance(event.getMessageObj().get("message").toString());
                if (okMessage.first) {
                    message = okMessage.second;
                } else {
                    message = new Message(event.getMessageObj().getJSONObject("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String senderVerkey = event.getMessageObj().optString("sender_verkey", null);
        String recipientVerkey = event.getMessageObj().optString("recipient_verkey", null);
        return new GetOneResult(message, senderVerkey, recipientVerkey);
    }

    public void send(Message message) throws SiriusPendingOperation {
        if (!isSetup) {
            throw new SiriusPendingOperation("You must Setup protocol instance at first");
        }

        rpc.setTimeout(timeToLiveSec);
        setupContext(message);

        try {
            rpc.sendMessage(message, Collections.singletonList(theirVK), endpoint, myVerkey, routingKeys, false);
        } catch (SiriusConnectionClosed siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        } catch (SiriusRPCError siriusRPCError) {
            siriusRPCError.printStackTrace();
        } catch (SiriusInvalidPayloadStructure siriusInvalidPayloadStructure) {
            siriusInvalidPayloadStructure.printStackTrace();
        } finally {
            cleanupContext(message);
        }
    }

    public List<Pair<Boolean, String>> sendMany(Message message, List<Pairwise> to) throws SiriusPendingOperation {
        List<RoutingBatch> batches = new ArrayList<>();
        for (Pairwise p : to) {
            batches.add(new RoutingBatch(Collections.singletonList(p.getTheir().getVerkey()), p.getTheir().getEndpoint(), p.getMe().getVerkey(), p.getTheir().getRoutingKeys()));
        }
        if (!isSetup) {
            throw new SiriusPendingOperation("You must Setup protocol instance at first");
        }
        rpc.setTimeout(timeToLiveSec);
        setupContext(message);
        try {
            return rpc.sendMessageBatched(message, batches);
        } catch (SiriusConnectionClosed siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }
}
