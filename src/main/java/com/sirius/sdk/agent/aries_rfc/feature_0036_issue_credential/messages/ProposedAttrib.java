package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages;

import org.json.JSONObject;

public class ProposedAttrib {
    JSONObject dict = new JSONObject();

    public ProposedAttrib(String name, String value) {
        dict.put("name", name);
        dict.put("value", value);
    }

    public ProposedAttrib(String name, String value, String mimeType) {
        dict.put("name", name);
        dict.put("value", value);
        dict.put("mime-type", mimeType);
    }

    public JSONObject getDict() {
        return dict;
    }
}
