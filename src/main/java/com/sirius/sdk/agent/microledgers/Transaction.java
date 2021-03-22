package com.sirius.sdk.agent.microledgers;

import org.json.JSONObject;

public class Transaction {
    public static final String METADATA_ATTR = "txnMetadata";
    public static final String ATTR_TIME = "txnTime";

    JSONObject payload = null;

    public Transaction(JSONObject obj) {
        payload = new JSONObject(obj.toString());
    }

    public JSONObject getJSONObject() {
        return payload;
    }

    public boolean hasMetadata() {
        return payload.has(METADATA_ATTR) && !payload.optJSONObject(METADATA_ATTR).isEmpty();
    }

    public String getTime() {
        JSONObject metadata = payload.optJSONObject(METADATA_ATTR);
        if (metadata != null) {
            return metadata.optString(ATTR_TIME, null);
        }
        return null;
    }
}
