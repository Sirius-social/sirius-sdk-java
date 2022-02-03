package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages;

import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class CredentialAck extends Ack {

    static {
        Message.registerMessageClass(CredentialAck.class, BaseIssueCredentialMessage.PROTOCOL, "ack");
    }

    public CredentialAck(String message) {
        super(message);
    }

    public static Builder<?> builder() {
        return new CredentialAckBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends Ack.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            return super.generateJSON();
        }

        public CredentialAck build() {
            return new CredentialAck(generateJSON().toString());
        }
    }

    private static class CredentialAckBuilder extends Builder<CredentialAckBuilder> {
        @Override
        protected CredentialAckBuilder self() {
            return this;
        }
    }
}
