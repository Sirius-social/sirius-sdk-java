package com.sirius.sdk.agent.model.coprotocols;

import com.sirius.sdk.agent.AgentRPC;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

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

    private static final String PLEASE_ACK_DECORATOR = "~please_ack";

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
    List<String> protocols;
    int timeToLiveSec = 30;
    Date dieTimestamp = null;
    List<String> pleaseAckIds = new ArrayList<>();

    /**
     * Should be called in Descendant
     * @param theirVerkey
     * @param endpoint
     * @param myVerkey
     * @param routing_keys
     */
    public void  setup(String theirVerkey, String endpoint, String myVerkey, List<String> routing_keys){
        this.theirVK = theirVerkey;
        this.myVerkey= myVerkey;
        this.endpoint = endpoint;
        this.routingKeys = routing_keys;
        if(routingKeys==null) {
            routingKeys = new ArrayList<>();
        }
        isSetup = true;
    }

    public boolean isStarted() {
        return started;
    }

    public void start(List<String> protocols) {
        this.dieTimestamp = null;
        this.protocols = protocols;
        started = true;
    }

    public void start(List<String> protocols, int timeToLiveSec) {
        start(protocols);
        this.dieTimestamp = new Date(System.currentTimeMillis() + this.timeToLiveSec * 1000L);
    }

    public void stop() {
        this.dieTimestamp = null;
        started = false;
        cleanupContext();
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

    public Pair<Boolean, Message> wait(Message message) throws SiriusPendingOperation {
        if (!isSetup) {
            throw new SiriusPendingOperation("You must Setup protocol instance at first");
        }

        rpc.setTimeout(timeToLiveSec);
        setupContext(message);
    }
}
