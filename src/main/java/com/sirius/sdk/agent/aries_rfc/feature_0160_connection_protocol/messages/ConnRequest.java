package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class ConnRequest extends ConnProtocolMessage {

    static {
        Message.registerMessageClass(ConnRequest.class, ConnProtocolMessage.PROTOCOL, "request");
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

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (label != null) {
                jsonObject.put("label", label);
            }

            if (did != null && verkey != null && endpoint != null) {
                JSONObject extra = (didDocExtra != null) ? didDocExtra : new JSONObject();
                jsonObject.put("connection", (new JSONObject().put("DID", did).put("DIDDoc", buildDidDoc(did, verkey, endpoint))));
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
