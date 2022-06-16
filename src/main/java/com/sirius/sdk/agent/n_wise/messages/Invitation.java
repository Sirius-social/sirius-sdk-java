package com.sirius.sdk.agent.n_wise.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

import java.util.List;

public class Invitation extends BaseNWiseMessage {

    static {
        Message.registerMessageClass(Invitation.class, BaseNWiseMessage.PROTOCOL, "invitation");
    }

    public Invitation(String msg) {
        super(msg);
    }

    public String getInviterVerkey() {
        return null;
    }

    public static Invitation.Builder<?> builder() {
        return new Invitation.InvitationBuilder();
    }

    public static abstract class Builder<B extends Invitation.Builder<B>> extends BaseNWiseMessage.Builder<B> {
        String label = null;
        String inviterKey = null;
        String endpoint = null;
        List<String> routingKeys = null;

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

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            put(label, "label", jsonObject);
            put(inviterKey, "inviterKey", jsonObject);
            put(endpoint, "serviceEndpoint", jsonObject);
            put(routingKeys, "routingKeys", jsonObject);

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
