package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConnRequest extends ConnProtocolMessage {

    static {
        Message.registerMessageClass(ConnRequest.class, ConnProtocolMessage.PROTOCOL, "request");
    }

    public String getLabel() {
        return getMessageObj().optString("label");
    }

    public ConnRequest(String msg) {
        super(msg);
    }

    public static Builder<?> builder() {
        return new ConnRequestBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends ConnProtocolMessage.Builder<B> {
        String label = null;
        String did = null;
        String verkey = null;
        String endpoint = null;
        JSONObject didDocExtra = null;
        List<JSONObject> connectionServices = new ArrayList<>();

        public B addConnectionService(JSONObject service) {
            connectionServices.add(service);
            return self();
        };

        public B setLabel(String label) {
            this.label = label;
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

        public B setDidDocExtra(JSONObject didDocExtra) {
            this.didDocExtra = didDocExtra;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            String id = jsonObject.optString("id");

            if (label != null) {
                jsonObject.put("label", label);
            }

            if (did != null && verkey != null && endpoint != null) {
                JSONObject extra = (didDocExtra != null) ? didDocExtra : new JSONObject();
                jsonObject.put("connection", (new JSONObject().
                        put("DID", did).
                        put("DIDDoc", buildDidDoc(did, verkey, endpoint, extra))));
                for (JSONObject s : connectionServices) {
                    jsonObject.getJSONObject("connection").getJSONObject("DIDDoc").getJSONArray("service").put(s);
                }
            }

            return jsonObject;
        }

        public ConnRequest build() {
            return new ConnRequest(generateJSON().toString());
        }
    }

    private static class ConnRequestBuilder extends Builder<ConnRequestBuilder> {
        @Override
        protected ConnRequestBuilder self() {
            return this;
        }
    }
}
