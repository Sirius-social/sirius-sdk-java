package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages;

import com.sirius.sdk.agent.aries_rfc.Utils;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

public class ConnResponse extends ConnProtocolMessage {

    static {
        Message.registerMessageClass(ConnResponse.class, ConnProtocolMessage.PROTOCOL, "response");
    }

    public ConnResponse(String msg) {
        super(msg);
    }

    static JSONObject signField(AbstractCrypto crypto, JSONObject fieldValue, String myVerkey) {
        return Utils.sign(crypto, fieldValue, myVerkey);
    }

    static JSONObject verifySignedField(AbstractCrypto crypto, JSONObject signedField) {
        Pair<JSONObject, Boolean> res = Utils.verifySigned(crypto, signedField);
        if (res.second) {
            return res.first;
        } else {
            return null;
        }
    }

    public void signConnection(AbstractCrypto crypto, String key) {
        JSONObject obj = getMessageObj();
        obj.put("connection~sig", signField(crypto, obj.getJSONObject("connection"), key));
        obj.remove("connection");
    }

    public boolean verifyConnection(AbstractCrypto crypto) {
        JSONObject connection = verifySignedField(crypto, getMessageObj().optJSONObject("connection~sig"));
        if (connection != null) {
            getMessageObj().put("connection", connection);
            return true;
        }
        return false;
    }

    public static Builder<?> builder() {
        return new ConnResponseBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends ConnProtocolMessage.Builder<B> {
        String did = null;
        String verkey = null;
        String endpoint = null;
        JSONObject didDocExtra = null;

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

        public B setDidDocExtra(JSONObject didDocExtra) {
            this.didDocExtra = didDocExtra;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            String id = generateId();
            jsonObject.put("@id", id);

            if (did != null && verkey != null && endpoint != null) {
                JSONObject extra = (didDocExtra != null) ? didDocExtra : new JSONObject();
                jsonObject.put("connection", (new JSONObject().
                        put("DID", did).
                        put("DIDDoc", buildDidDoc(did, verkey, endpoint, extra))));
            }

            return jsonObject;
        }

        public ConnResponse build() {
            return new ConnResponse(generateJSON().toString());
        }
    }

    private static class ConnResponseBuilder extends Builder<ConnResponseBuilder> {
        @Override
        protected ConnResponseBuilder self() {
            return this;
        }
    }
}
