package com.sirius.sdk.agent.n_wise.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class Response extends BaseNWiseMessage {

    static {
        Message.registerMessageClass(Response.class, BaseNWiseMessage.PROTOCOL, "response");
    }
    public Response(String msg) {
        super(msg);
    }

    public JSONObject getAttach() {
        return getMessageObj().getJSONObject("attach");
    }

    public static Builder<?> builder() {
        return new ResponseBuilder();
    }

    public static abstract class Builder<B extends Response.Builder<B>> extends BaseNWiseMessage.Builder<B> {
        String ledgerType = null;
        JSONObject attach = null;

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

            put(ledgerType, "ledgerType", jsonObject);
            put(attach, "attach", jsonObject);

            return jsonObject;
        }

        public Response build() {
            return new Response(generateJSON().toString());
        }

    }

    private static class ResponseBuilder extends Response.Builder<ResponseBuilder> {
        @Override
        protected ResponseBuilder self() {
            return this;
        }
    }
}
