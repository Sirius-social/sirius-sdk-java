package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Invitation extends ConnProtocolMessage {

    static {
        Message.registerMessageClass(Invitation.class, ConnProtocolMessage.PROTOCOL, "invitation");
    }

    public Invitation(String msg) {
        super(msg);
    }

    public static Invitation.Builder<?> builder() {
        return new Invitation.InvitationBuilder();
    }

    public List<String> recipientKeys() {
        List<String> res = new ArrayList<>();
        if (getMessageObj().has("recipientKeys")) {
            JSONArray jsonArr = getMessageObj().getJSONArray("recipientKeys");
            for (Object obj : jsonArr) {
                res.add((String) obj);
            }
        }
        return res;
    }

    public String endpoint() {
        return getMessageObj().optString("serviceEndpoint");
    }

    public String label() {
        return getMessageObj().optString("label");
    }

    @Override
    public boolean validate() {
        if (super.validate()) {
            return getMessageObj().has("label") &&
                    getMessageObj().has("recipientKeys") &&
                    getMessageObj().has("serviceEndpoint");
        }
        return false;
    }

    public static abstract class Builder<B extends Builder<B>> extends ConnProtocolMessage.Builder<B> {
        String label = null;
        List<String> recipientKeys = null;
        String endpoint = null;
        List<String> routingKeys = null;
        String did = null;

        public B setDid(String did) {
            this.did = did;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (label != null) {
                jsonObject.put("label", label);
            }
            if (recipientKeys != null) {
                jsonObject.put("recipientKeys", recipientKeys);
            }
            if (endpoint != null) {
                jsonObject.put("serviceEndpoint", endpoint);
            }
            if (routingKeys != null) {
                jsonObject.put("routingKeys", routingKeys);
            }
            if (did != null) {
                jsonObject.put("did", did);
            }

            return jsonObject;
        }

        public Invitation build() {
            return new Invitation(generateJSON().toString());
        }
    }

    private static class InvitationBuilder extends Builder<InvitationBuilder> {
        @Override
        protected InvitationBuilder self() {
            return this;
        }
    }


}
