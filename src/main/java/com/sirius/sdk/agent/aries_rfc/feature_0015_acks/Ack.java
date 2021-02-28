package com.sirius.sdk.agent.aries_rfc.feature_0015_acks;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;


public class Ack extends AriesProtocolMessage {

    public static final String PROTOCOL = "notification";

    static {
        Message.registerMessageClass(Ack.class, PROTOCOL, "ack");
    }

    public Ack(String message) {
        super(message);
    }

    public enum Status {
        OK,
        PENDING,
        FAIL
    }

    public static Builder<?> builder() {
        return new AckBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProtocolMessage.Builder<B> {
        Status status = null;

        public B setStatus(Status status) {
            this.status = status;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            String id = generateId();
            jsonObject.put("@id", id);

            if (status != null) {
                jsonObject.put("status", status.name());
            }

            return jsonObject;
        }

        public Ack build() {
            return new Ack(generateJSON().toString());
        }
    }

    private static class AckBuilder extends Builder<AckBuilder> {
        @Override
        protected AckBuilder self() {
            return this;
        }
    }
}
