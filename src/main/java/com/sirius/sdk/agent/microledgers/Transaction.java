package com.sirius.sdk.agent.microledgers;

import org.json.JSONObject;

public class Transaction extends JSONObject {
    public static final String METADATA_ATTR = "txnMetadata";
    public static final String ATTR_TIME = "txnTime";

    public Transaction(JSONObject obj) {
        super(obj.toString());
    }

    public boolean hasMetadata() {
        return has(METADATA_ATTR) && !optJSONObject(METADATA_ATTR).isEmpty();
    }

    public String getTime() {
        JSONObject metadata = optJSONObject(METADATA_ATTR);
        if (metadata != null) {
            return metadata.optString(ATTR_TIME, null);
        }
        return null;
    }
}
