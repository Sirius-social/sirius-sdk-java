package com.sirius.sdk.agent.aries_rfc;

import org.json.JSONArray;
import org.json.JSONObject;

public class DidDoc {
    public static final String DID = "did";
    public static final String DID_DOC = "did_doc";
    public static final String VCX_DID = "DID";
    public static final String VCX_DID_DOC = "DIDDoc";

    JSONObject payload;

    public DidDoc(JSONObject payload) {
        this.payload = payload;
    }

    public JSONObject getPayload() {
        return payload;
    }

    public JSONObject extractService(boolean highPriority, String type) {
        JSONArray services = payload.optJSONArray("service");
        if (services != null) {
            JSONObject ret = null;
            for (Object serviceObj : services) {
                JSONObject service = (JSONObject) serviceObj;
                if (!service.optString("type").equals(type))
                    continue;
                if (ret == null) {
                    ret = service;
                } else {
                    if (highPriority) {
                        if (service.optInt("priority", 0) > ret.optInt("priority", 0)) {
                            ret = service;
                        }
                    }
                }
            }
            return ret;
        }

        return null;
    }

    public JSONObject extractService() {
        return extractService(true, "IndyAgent");
    }
}
