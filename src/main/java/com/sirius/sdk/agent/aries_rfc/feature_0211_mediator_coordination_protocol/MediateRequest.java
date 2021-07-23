package com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol;

import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;


public class MediateRequest extends CoordinateMediationMessage {

    static {
        Message.registerMessageClass(MediateRequest.class, CoordinateMediationMessage.PROTOCOL, "mediate-request");
    }

    public MediateRequest(String message) {
        super(message);
    }

    public static Builder<?> builder() {
        return new MediateRequestMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends CoordinateMediationMessage.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            return super.generateJSON();
        }

        public MediateRequest build() {
            return new MediateRequest(generateJSON().toString());
        }
    }

    private static class MediateRequestMessageBuilder extends Builder<MediateRequestMessageBuilder> {
        @Override
        protected MediateRequestMessageBuilder self() {
            return this;
        }
    }
}
