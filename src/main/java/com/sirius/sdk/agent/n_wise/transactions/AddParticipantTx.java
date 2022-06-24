package com.sirius.sdk.agent.n_wise.transactions;

import org.json.JSONObject;

public class AddParticipantTx extends JSONObject {


    public void setNickname(String nickname) {
        put("nickname", nickname);
    }

    public String getNickname() {
        return optString("nickname");
    }
    public void setDid(String did) {
        put("did", did);
    }

    public String getDid() {
        return optString("did");
    }

    public void setDidDoc(JSONObject didDoc) {
        put("didDoc", didDoc);
    }

    public JSONObject getDidDoc() {
        return getJSONObject("didDoc");
    }

    public void setRole(String role) {
        put("role", role);
    }

    public String getRole() {
        return optString("role");
    }
}
