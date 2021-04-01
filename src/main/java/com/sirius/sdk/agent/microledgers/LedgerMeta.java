package com.sirius.sdk.agent.microledgers;

import org.json.JSONObject;

public class LedgerMeta extends JSONObject {

    public LedgerMeta(JSONObject obj) {
        super(obj.toString());
    }

    public LedgerMeta(String name, String uid, String created) {
        super();
        put("name", name);
        put("uid", uid);
        put("created", created);
    }

    public String getName() {
        return optString("name");
    }

    public String getUid() {
        return optString("uid");
    }

    public String getCreated() {
        return optString("created");
    }
}
