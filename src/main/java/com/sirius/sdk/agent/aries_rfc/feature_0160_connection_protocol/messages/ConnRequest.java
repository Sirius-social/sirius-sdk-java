package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ConnRequest extends ConnProtocolMessage {

    static {
        Message.registerMessageClass(ConnRequest.class, ConnProtocolMessage.PROTOCOL, "request");
    }

    public class ExtractTheirInfoRes {
        public String did;
        public String verkey;
        public String endpoint;
        public List<String> routingKeys;
    }

    public String theirDid() {
        JSONObject obj = getMessageObj();
        if (obj.has("connection")) {
            if (obj.getJSONObject("connection").has("did")) {
                return obj.getJSONObject("connection").getString("did");
            }
            if (obj.getJSONObject("connection").has("DID")) {
                return obj.getJSONObject("connection").getString("DID");
            }
        }
        return "";
    }

    public DidDoc didDoc() {
        JSONObject obj = getMessageObj();
        if (obj.has("connection")) {
            if (obj.getJSONObject("connection").has("did_doc")) {
                return new DidDoc(obj.getJSONObject("connection").getJSONObject("did_doc"));
            }
            if (obj.getJSONObject("connection").has("DIDDoc")) {
                return new DidDoc(obj.getJSONObject("connection").getJSONObject("DIDDoc"));
            }
        }
        return null;
    }

    public ExtractTheirInfoRes extractTheirInfo() throws SiriusInvalidMessage {
        if (theirDid().isEmpty()) {
            throw new SiriusInvalidMessage("Connection metadata is empty");
        }
        if (didDoc() == null) {
            throw new SiriusInvalidMessage("DID Doc is empty");
        }

        JSONObject service = didDoc().extractService();
        String theirEndpoint = service.optString("serviceEndpoint");
        JSONArray publicKeys = didDoc().getPayload().getJSONArray("publicKey");

        String theirVk = extractKey(service.getJSONArray("recipientKeys").getString(0), publicKeys);

        List<String> routingKeys = new ArrayList<>();
        if (service.has("routingKeys")) {
            for (Object rkObj : service.getJSONArray("routingKeys")) {
                routingKeys.add(extractKey((String) rkObj, publicKeys));
            }
        }

        ExtractTheirInfoRes res = new ExtractTheirInfoRes();
        res.did = theirDid();
        res.verkey = theirVk;
        res.endpoint = theirEndpoint;
        res.routingKeys = routingKeys;
        return res;
    }

    private String extractKey(String name, JSONArray publicKeys) {
        if (name.contains("#")) {
            String[] splitRes = name.split("#");
            String controller = splitRes[0];
            String id = splitRes[1];
            for (Object keyObj : publicKeys) {
                JSONObject keyJson = (JSONObject) keyObj;
                if (keyJson.optString("controller").equals(controller) && keyJson.optString("id").equals(id)) {
                    return keyJson.optString("publicKeyBase58");
                }
            }
            return "";
        } else {
            return name;
        }
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

            if (label != null) {
                jsonObject.put("label", label);
            }

            if (did != null && verkey != null && endpoint != null) {
                JSONObject extra = (didDocExtra != null) ? didDocExtra : new JSONObject();
                jsonObject.put("connection", (new JSONObject().
                        put("DID", did).
                        put("DIDDoc", buildDidDoc(did, verkey, endpoint))));
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
