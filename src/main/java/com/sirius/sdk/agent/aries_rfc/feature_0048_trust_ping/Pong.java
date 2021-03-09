package com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import com.sirius.sdk.messaging.Message;

/**
 * Implementation of Pong part for trust_ping protocol
 * https://github.com/hyperledger/aries-rfcs/tree/master/features/0048-trust-ping
 */
public class Pong extends AriesProtocolMessage {

    public static final String PROTOCOL = "trust_ping";

    static {
        Message.registerMessageClass(Pong.class, Pong.PROTOCOL, "ping_response");
    }

    public String getComment() {
        return comment;
    }

    String comment;

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getThreadId() {
        return threadId;
    }

    String threadId;

    public Pong(String message) {
        super(message);
        comment = getStringFromJSON("comment");
        threadId = getJSONOBJECTFromJSON(THREAD_DECORATOR).getString("thid");
    }

    public String getPingId() {
        try {
            return getJSONOBJECTFromJSON(THREAD_DECORATOR).getString("thid");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
