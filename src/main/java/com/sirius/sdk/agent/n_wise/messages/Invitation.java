package com.sirius.sdk.agent.n_wise.messages;

import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Base58;
import org.json.JSONObject;

public class Invitation extends BaseNWiseMessage {

    static {
        Message.registerMessageClass(Invitation.class, BaseNWiseMessage.PROTOCOL, "invitation");
    }

    public Invitation(String msg) {
        super(msg);
    }

    public String getLabel() {
        return getMessageObj().optString("label");
    }

    public byte[] getInvitationPrivateKey() {
        return Base58.decode(getMessageObj().optString("invitationPrivateKeyBase58"));
    }

    public String getInvitationKeyId() {
        return getMessageObj().optString("invitationKeyId");
    }

    public String getLedgerType() {
        return getMessageObj().optString("ledgerType");
    }

    public JSONObject getAttach() {
        return getMessageObj().getJSONObject("attach");
    }

    public static Builder<?> builder() {
        return new InvitationBuilder();
    }

    public static abstract class Builder<B extends Invitation.Builder<B>> extends BaseNWiseMessage.Builder<B> {
        String label = null;
        String invitationKeyId = null;
        String invitationPrivateKeyBase58 = null;
        String ledgerType = null;
        JSONObject attach = null;

        public B setLabel(String label) {
            this.label = label;
            return self();
        }

        public B setInvitationKeyId(String keyId) {
            this.invitationKeyId = keyId;
            return self();
        }

        public B setInvitationPrivateKey(byte[] invitationPrivateKey) {
            this.invitationPrivateKeyBase58 = Base58.encode(invitationPrivateKey);
            return self();
        }

        public B setLedgerType(String ledgerType) {
            this.ledgerType = ledgerType;
            return self();
        }

        public B setAttach(JSONObject attach) {
            this.attach = attach;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            put(label, "label", jsonObject);
            put(invitationKeyId, "invitationKeyId", jsonObject);
            put(invitationPrivateKeyBase58, "invitationPrivateKeyBase58", jsonObject);
            put(ledgerType, "ledgerType", jsonObject);
            put(attach, "attach", jsonObject);

            return jsonObject;
        }

        public Invitation build() {
            return new Invitation(generateJSON().toString());
        }
    }

    private static class InvitationBuilder extends Invitation.Builder<InvitationBuilder> {
        @Override
        protected InvitationBuilder self() {
            return this;
        }
    }


}
