package com.sirius.sdk.agent.n_wise;

import org.bitcoinj.core.Base58;
import org.json.JSONObject;

public class NWiseParticipant {
    public String nickname;
    public String did;
    public JSONObject didDoc;

    public String getEndpoint() {
        return didDoc.optJSONArray("service").optJSONObject(0).optString("serviceEndpoint");
    }

    public byte[] getVerkey() {
        return Base58.decode(didDoc.optJSONArray("publicKey").optJSONObject(0).optString("publicKeyBase58"));
    }
}
