package com.sirius.sdk.agent.n_wise.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Invitation extends BaseNWiseMessage {

    static {
        Message.registerMessageClass(Invitation.class, BaseNWiseMessage.PROTOCOL, "invitation");
    }

    public Invitation(String msg) {
        super(msg);
    }

    public String getInviterVerkey() {
        return getMessageObj().optString("inviterKey");
    }

    public String getEndpoint() {
        return getMessageObj().optString("serviceEndpoint");
    }

    public String getLedgerType() {
        return getMessageObj().optString("ledgerType");
    }

    public List<String> routingKeys() {
        List<String> res = new ArrayList<>();
        if (getMessageObj().has("routingKeys")) {
            JSONArray jsonArr = getMessageObj().getJSONArray("routingKeys");
            for (Object obj : jsonArr) {
                res.add((String) obj);
            }
        }
        return res;
    }

    public static Builder<?> builder() {
        return new Invitation.InvitationBuilder();
    }

    public static abstract class Builder<B extends Invitation.Builder<B>> extends BaseNWiseMessage.Builder<B> {
        String label = null;
        String inviterKey = null;
        String endpoint = null;
        List<String> routingKeys = null;
        String ledgerType = null;

        public B setLabel(String label) {
            this.label = label;
            return self();
        }

        public B setInviterKey(String inviterKey) {
            this.inviterKey = inviterKey;
            return self();
        }

        public B setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return self();
        }

        public B setRoutingKeys(List<String> routingKeys) {
            this.routingKeys = routingKeys;
            return self();
        }

        public B setLedgerType(String ledgerType) {
            this.ledgerType = ledgerType;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            put(label, "label", jsonObject);
            put(inviterKey, "inviterKey", jsonObject);
            put(endpoint, "serviceEndpoint", jsonObject);
            put(routingKeys, "routingKeys", jsonObject);
            put(ledgerType, "ledgerType", jsonObject);

            return jsonObject;
        }

        public Invitation build() {
            return new Invitation(generateJSON().toString());
        }
    }

    private static class InvitationBuilder extends Invitation.Builder<Invitation.InvitationBuilder> {
        @Override
        protected Invitation.InvitationBuilder self() {
            return this;
        }
    }


}
