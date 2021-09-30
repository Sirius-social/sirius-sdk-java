package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class ConnProtocolMessage extends AriesProtocolMessage {
    public static final String PROTOCOL = "connections";

    public ConnProtocolMessage(String msg) {
        super(msg);
    }

    public static class ExtractTheirInfoRes {
        public String did;
        public String verkey;
        public String endpoint;
        public List<String> routingKeys;
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

    public static abstract class Builder<B extends ConnProtocolMessage.Builder<B>> extends AriesProtocolMessage.Builder<B> {

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            return super.generateJSON();
        }
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

    public static JSONObject buildDidDoc(String did, String verkey, String endpoint, JSONObject extra) {
        String keyId = did + "#1";
        JSONObject doc = (new JSONObject()).
                put("@context", "https://w3id.org/did/v1").
                put("id", did).
                put("authentication", (new JSONArray()).
                        put((new JSONObject()).
                                put("publicKey", keyId).
                                put("type", "Ed25519SignatureAuthentication2018"))).
                put("publicKey", (new JSONArray()).
                        put((new JSONObject()).
                                put("id", "1").
                                put("type", "Ed25519VerificationKey2018").
                                put("controller", did).
                                put("publicKeyBase58", verkey))).
                put("service", (new JSONArray())
                        .put((new JSONObject()).
                                put("id", "did:peer:" + did + ";indy").
                                put("type", "IndyAgent").
                                put("priority", 0).
                                //put("recipientKeys", (new JSONArray()).put(keyId)).
                                put("recipientKeys", (new JSONArray()).put(verkey)).
                                put("serviceEndpoint", endpoint)));
        for (String key : extra.keySet()) {
            doc.put(key, extra.get(key));
        }
        return doc;
    }

    public static JSONObject buildDidDoc(String did, String verkey, String endpoint) {
        return buildDidDoc(did, verkey, endpoint, new JSONObject());
    }
}
