package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONArray;
import org.json.JSONObject;

public class ConnProtocolMessage extends AriesProtocolMessage {
    public static final String PROTOCOL = "connections";

    public ConnProtocolMessage(String msg) {
        super(msg);
    }

    public static abstract class Builder<B extends ConnProtocolMessage.Builder<B>> extends AriesProtocolMessage.Builder<B> {

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }
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
                                put("recipientKeys", (new JSONArray()).put(keyId))
                                .put("serviceEndpoint", endpoint)));
        for (String key : extra.keySet()) {
            doc.put(key, extra.get(key));
        }
        return doc;
    }

    public static JSONObject buildDidDoc(String did, String verkey, String endpoint) {
        return buildDidDoc(did, verkey, endpoint, new JSONObject());
    }
}
