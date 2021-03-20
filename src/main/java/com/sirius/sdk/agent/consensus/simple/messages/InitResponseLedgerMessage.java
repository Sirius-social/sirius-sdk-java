package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class InitResponseLedgerMessage extends InitRequestLedgerMessage {

    static {
        Message.registerMessageClass(InitResponseLedgerMessage.class, SimpleConsensusMessage.PROTOCOL, "initialize-response");
    }

    public InitResponseLedgerMessage(String msg) {
        super(msg);
    }

    public void assignFrom(BaseInitLedgerMessage source) {
        for (String key : source.getMessageObj().keySet()) {
            if (key.equals(FIELD_ID) || key.equals(FIELD_TYPE) || key.equals(THREAD_DECORATOR))
                continue;
            this.getMessageObj().put(key, source.getMessageObj().get(key));
        }
    }

    public JSONObject signature(String did) {
        for (Object o : this.signatures()) {
            if (((JSONObject)o).optString("participant").equals(did)) {
                return (JSONObject)o;
            }
        }
        return null;
    }

    public static Builder<?> builder() {
        return new InitResponseLedgerMessage.InitResponseLedgerMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends InitRequestLedgerMessage.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public InitResponseLedgerMessage build() {
            return new InitResponseLedgerMessage(generateJSON().toString());
        }
    }

    private static class InitResponseLedgerMessageBuilder extends Builder<InitResponseLedgerMessageBuilder> {
        @Override
        protected InitResponseLedgerMessageBuilder self() {
            return this;
        }
    }
}
