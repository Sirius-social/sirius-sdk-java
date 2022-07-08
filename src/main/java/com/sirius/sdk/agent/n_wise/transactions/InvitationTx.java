package com.sirius.sdk.agent.n_wise.transactions;

import com.sirius.sdk.utils.Base58;
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
        List<String> keysBase58 = new ArrayList<>();
        for (byte[] b : keys) {
            keysBase58.add(Base58.encode(b));
        }
        put("keysBase58", keysBase58);
    }
}
