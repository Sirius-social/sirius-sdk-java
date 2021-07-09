package com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol;

import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;


public class MediateDeny extends CoordinateMediationMessage {

    static {
        Message.registerMessageClass(MediateDeny.class, CoordinateMediationMessage.PROTOCOL, "mediate-request");
    }

    public MediateDeny(String message) {
        super(message);
    }

    public static Builder<?> builder() {
        return new MediateDenyMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends CoordinateMediationMessage.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public MediateDeny build() {
            return new MediateDeny(generateJSON().toString());
        }
    }

    private static class MediateDenyMessageBuilder extends Builder<MediateDenyMessageBuilder> {
        @Override
        protected MediateDenyMessageBuilder self() {
            return this;
        }
    }
}
