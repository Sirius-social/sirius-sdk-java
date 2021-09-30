package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages;

import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class PresentationAck extends Ack {

    static {
        Message.registerMessageClass(PresentationAck.class, BasePresentProofMessage.PROTOCOL, "ack");
    }

    public PresentationAck(String message) {
        super(message);
    }

    public static Builder<?> builder() {
        return new PresentationAckBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends Ack.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            return super.generateJSON();
        }

        public PresentationAck build() {
            return new PresentationAck(generateJSON().toString());
        }
    }

    private static class PresentationAckBuilder extends Builder<PresentationAckBuilder> {
        @Override
        protected PresentationAckBuilder self() {
            return this;
        }
    }
}
