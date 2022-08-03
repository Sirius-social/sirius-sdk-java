package com.sirius.sdk.agent.n_wise.transactions;

import com.sirius.sdk.utils.Base58;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InvitationTx extends NWiseTx {

    public InvitationTx() {
        super();
        put("type", "invitationTx");
    }

    public InvitationTx(JSONObject jsonObject) {
        super(jsonObject.toString());
    }

    public void setPublicKeys(List<byte[]> keys) {
        JSONArray jsonKeys = new JSONArray();
        for (byte[] b : keys) {
            jsonKeys.put(
                    new JSONObject().
                    put("id", Base58.encode(b)).
                    put("type", "Ed25519VerificationKey2018").
                    put("publicKeyBase58", Base58.encode(b))
            );
        }
        put("publicKey", jsonKeys);
    }

    public List<Pair<String, byte[]>> getPublicKeys() {
        List<Pair<String, byte[]>> res = new ArrayList<>();
        JSONArray jsonKeys = optJSONArray("publicKey");
        for (Object o : jsonKeys) {
            JSONObject jsonObject = (JSONObject) o;
            res.add(new Pair<>(jsonObject.optString("id"), Base58.decode(jsonObject.optString("publicKeyBase58"))));
        }
        return res;
    }
}
