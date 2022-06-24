package com.sirius.sdk.agent.n_wise.messages;

import org.json.JSONObject;

public class IotaResponseAttach extends JSONObject {

    public IotaResponseAttach(String tag) {
        super();
        put("tag", tag);
    }

    public IotaResponseAttach(JSONObject o) {
        super(o.toString());
    }

    public String getTag() {
        return getString("tag");
    }
}
