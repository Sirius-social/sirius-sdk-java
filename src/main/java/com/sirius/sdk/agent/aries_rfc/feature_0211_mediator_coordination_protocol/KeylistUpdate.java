package com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol;

import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class KeylistUpdate extends CoordinateMediationMessage {

    static {
        Message.registerMessageClass(KeylistUpdate.class, CoordinateMediationMessage.PROTOCOL, "keylist-update");
    }

    public KeylistUpdate(String message) {
        super(message);
    }

    public static Builder<?> builder() {
        return new KeylistUpdateMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends CoordinateMediationMessage.Builder<B> {

        JSONArray updates = new JSONArray();

        public B addKey(String key) {
            updates.put(new JSONObject().
                    put("action", "add").
                    put("recipient_key", key));
            return self();
        }

        public B addKeys(List<String> keys) {
            for (String k : keys)
                self().addKey(k);
            return self();
        }

        public B removeKey(String key) {
            updates.put(new JSONObject().
                    put("action", "remove").
                    put("recipient_key", key));
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            jsonObject.put("updates", updates);
            return jsonObject;
        }

        public KeylistUpdate build() {
            return new KeylistUpdate(generateJSON().toString());
        }
    }

    private static class KeylistUpdateMessageBuilder extends Builder<KeylistUpdateMessageBuilder> {
        @Override
        protected KeylistUpdateMessageBuilder self() {
            return this;
        }
    }
}
