package com.sirius.sdk.agent.n_wise.transactions;

import org.json.JSONObject;

public class RemoveParticipantTx extends NWiseTx {
    public RemoveParticipantTx() {
        super();
        put("type", "removeParticipantTx");
    }

    public RemoveParticipantTx(JSONObject o) {
        super(o.toString());
    }

    public void setDid(String did) {
        put("did", did);
    }

    public String getDid() {
        return optString("did");
    }
}
