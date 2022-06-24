package com.sirius.sdk.agent.n_wise.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class Request extends BaseNWiseMessage {

    static {
        Message.registerMessageClass(Request.class, BaseNWiseMessage.PROTOCOL, "request");
    }
    public Request(String msg) {
        super(msg);
    }

    public static Builder<?> builder() {
        return new RequestBuilder();
    }

    public static abstract class Builder<B extends Request.Builder<B>> extends BaseNWiseMessage.Builder<B> {
        String nickname = null;
        String did = null;
        String verkey = null;
        String endpoint = null;

        public B setNickname(String nickname) {
            this.nickname = nickname;
            return self();
        }

        public B setDid(String did) {
            this.did = did;
            return self();
        }

        public B setVerkey(String verkey) {
            this.verkey = verkey;
            return self();
        }

        public B setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            put(nickname, "nickname", jsonObject);
            put(did, "did", jsonObject);
            put(verkey, "verkey", jsonObject);
            put(endpoint, "endpoint", jsonObject);

            return jsonObject;
        }

        public Request build() {
            return new Request(generateJSON().toString());
        }
    }

    private static class RequestBuilder extends Request.Builder<RequestBuilder> {
        @Override
        protected RequestBuilder self() {
            return this;
        }
    }


}
