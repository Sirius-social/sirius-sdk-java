package com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;


/**Implementation of Ping part for trust_ping protocol
 *  https://github.com/hyperledger/aries-rfcs/tree/master/features/0048-trust-ping
 */
public class Ping extends AriesProtocolMessage {
    public static final String PROTOCOL = "trust_ping";

    static {
        Message.registerMessageClass(Ping.class, Ping.PROTOCOL, "ping");
    }

    public String getComment() {
        return getStringFromJSON("comment");
    }

    public Boolean getResponseRequested() {
        return getBooleanFromJSON("response_requested");
    }

    public Ping(String message) {
        super(message);
    }

    public static Builder<?> builder() {
        return new PingBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProtocolMessage.Builder<B> {
        String comment = null;
        Boolean responseRequested = null;

        public B setComment(String comment) {
            this.comment = comment;
            return self();
        }

        public B setResponseRequested(boolean responseRequested) {
            this.responseRequested = responseRequested;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (comment != null) {
                jsonObject.put("comment", comment);
            }

            if (responseRequested != null) {
                jsonObject.put("response_requested", responseRequested);
            }

            return jsonObject;
        }

        public Ping build() {
            return new Ping(generateJSON().toString());
        }
    }

    private static class PingBuilder extends Builder<PingBuilder> {
        @Override
        protected PingBuilder self() {
            return this;
        }
    }
}
