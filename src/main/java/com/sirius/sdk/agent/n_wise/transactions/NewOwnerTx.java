package com.sirius.sdk.agent.n_wise.transactions;

import org.json.JSONObject;

public class NewOwnerTx extends NWiseTx {

    public NewOwnerTx() {
        super();
        put("type", "newOwnerTx");
    }

    public NewOwnerTx(JSONObject o) {
        super(o.toString());
    }

    public void setDid(String did) {
        put("did", did);
    }

    public String getDid() {
        return optString("did");
    }
}
