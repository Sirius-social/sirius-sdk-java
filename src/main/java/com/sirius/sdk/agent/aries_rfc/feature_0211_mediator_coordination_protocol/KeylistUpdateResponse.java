package com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol;

import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class KeylistUpdateResponse extends CoordinateMediationMessage {

    static {
        Message.registerMessageClass(KeylistUpdateResponse.class, CoordinateMediationMessage.PROTOCOL, "keylist-update-response");
    }

    public KeylistUpdateResponse(String message) {
        super(message);
    }

    public static Builder<?> builder() {
        return new KeylistResponseMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends CoordinateMediationMessage.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            return super.generateJSON();
        }

        public KeylistUpdateResponse build() {
            return new KeylistUpdateResponse(generateJSON().toString());
        }
    }

    private static class KeylistResponseMessageBuilder extends Builder<KeylistResponseMessageBuilder> {
        @Override
        protected KeylistResponseMessageBuilder self() {
            return this;
        }
    }
}
