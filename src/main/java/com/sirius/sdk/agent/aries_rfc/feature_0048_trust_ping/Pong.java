package com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

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
        return getStringFromJSON("comment");
    }

    public Pong(String message) {
        super(message);
    }

    public static Builder<?> builder() {
        return new PongBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProtocolMessage.Builder<B> {
        String comment = null;
        String pingId = null;

        public B setComment(String comment) {
            this.comment = comment;
            return self();
        }

        public B setPingId(String pingId) {
            this.pingId = pingId;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (comment != null) {
                jsonObject.put("comment", comment);
            }

            if (pingId != null) {
                JSONObject thread = jsonObject.optJSONObject(THREAD_DECORATOR);
                if (thread == null)
                    thread = new JSONObject();
                thread.put("thid", pingId);
                jsonObject.put(THREAD_DECORATOR, thread);
            }

            return jsonObject;
        }

        public Pong build() {
            return new Pong(generateJSON().toString());
        }
    }

    private static class PongBuilder extends Builder<PongBuilder> {
        @Override
        protected PongBuilder self() {
            return this;
        }
    }
}
