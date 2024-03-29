package com.sirius.sdk.agent.n_wise.transactions;


import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import static com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProtocolMessage.buildDidDoc;

public class GenesisTx extends NWiseTx {

    public GenesisTx() {
        super();
        put("type", "genesisTx");
    }

    public GenesisTx(JSONObject jsonObject) {
        super(jsonObject.toString());
    }

    public String getLabel() {
        return optString("label");
    }

    public void setLabel(String label) {
        put("label", label);
    }

    public String getCreatorNickname() {
        return optString("creatorNickname");
    }

    public void setCreatorNickname(String creatorNickname) {
        put("creatorNickname", creatorNickname);
    }

    public String getCreatorDid() {
        return getJSONObject("connection").optString("DID");
    }

    public byte[] getCreatorVerkey() {
        return Base58.decode(getJSONObject("connection").optJSONObject("DIDDoc").
                optJSONArray("publicKey").getJSONObject(0).optString("publicKeyBase58"));
    }

    public JSONObject getCreatorDidDoc() {
        return getJSONObject("connection").optJSONObject("DIDDoc");
    }

    public void setCreatorDidDocParams(String did, byte[] verkey, String endpoint, List<JSONObject> connectionServices, JSONObject didDocExtra) {
        put("connection", (new JSONObject().
                put("DID", did).
                put("DIDDoc", buildDidDoc(did, Base58.encode(verkey), endpoint, didDocExtra))));
        for (JSONObject s : connectionServices) {
            getJSONObject("connection").getJSONObject("DIDDoc").getJSONArray("service").put(s);
        }
    }

    public void setCreatorDidDocParams(String did, byte[] verkey, String endpoint) {
        setCreatorDidDocParams(did, verkey, endpoint, Arrays.asList(), new JSONObject());
    }
}
