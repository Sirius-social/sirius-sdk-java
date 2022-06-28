package com.sirius.sdk.agent.n_wise.messages;

import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Base58;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProtocolMessage.buildDidDoc;

public class Request extends BaseNWiseMessage {

    static {
        Message.registerMessageClass(Request.class, BaseNWiseMessage.PROTOCOL, "request");
    }
    public Request(String msg) {
        super(msg);
    }

    public String getNickname() {
        return getMessageObj().optString("nickname");
    }

    public String getDid() {
        return getMessageObj().optJSONObject("connection").optString("DID");
    }

    public JSONObject getDidDoc() {
        return getMessageObj().optJSONObject("connection").optJSONObject("DIDDoc");
    }

    public String getEndpoint() {
        return getDidDoc().getJSONArray("service").getJSONObject(0).getString("serviceEndpoint");
    }

    public byte[] getVerkey() {
        return Base58.decode(getDidDoc().optJSONArray("publicKey").getJSONObject(0).optString("publicKeyBase58"));
    }

    public static Builder<?> builder() {
        return new RequestBuilder();
    }

    public static abstract class Builder<B extends Request.Builder<B>> extends BaseNWiseMessage.Builder<B> {
        String nickname = null;
        String did = null;
        byte[] verkey = null;
        String endpoint = null;
        JSONObject didDocExtra = null;
        List<JSONObject> connectionServices = new ArrayList<>();

        public B setNickname(String nickname) {
            this.nickname = nickname;
            return self();
        }

        public B setDid(String did) {
            this.did = did;
            return self();
        }

        public B setVerkey(byte[] verkey) {
            this.verkey = verkey;
            return self();
        }

        public B setEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return self();
        }

        public B setDidDocExtra(JSONObject didDocExtra) {
            this.didDocExtra = didDocExtra;
            return self();
        }

        public B addConnectionService(JSONObject service) {
            connectionServices.add(service);
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            put(nickname, "nickname", jsonObject);

            if (did != null && verkey != null && endpoint != null) {
                JSONObject extra = (didDocExtra != null) ? didDocExtra : new JSONObject();
                jsonObject.put("connection", (new JSONObject().
                        put("DID", did).
                        put("DIDDoc", buildDidDoc(did, Base58.encode(verkey), endpoint, extra))));
                for (JSONObject s : connectionServices) {
                    jsonObject.getJSONObject("connection").getJSONObject("DIDDoc").getJSONArray("service").put(s);
                }
            }

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
