package com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol;

import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;


public class MediateGrant extends CoordinateMediationMessage {

    static {
        Message.registerMessageClass(MediateGrant.class, CoordinateMediationMessage.PROTOCOL, "mediate-request");
    }

    public MediateGrant(String message) {
        super(message);
    }

    public static Builder<?> builder() {
        return new MediateGrantMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends CoordinateMediationMessage.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public MediateGrant build() {
            return new MediateGrant(generateJSON().toString());
        }
    }

    private static class MediateGrantMessageBuilder extends Builder<MediateGrantMessageBuilder> {
        @Override
        protected MediateGrantMessageBuilder self() {
            return this;
        }
    }
}
