package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import org.json.JSONObject;

public class AttribTranslation {
    JSONObject dict = new JSONObject();

    public AttribTranslation(String attribName, String translation) {
        dict.put("attrib_name", attribName);
        dict.put("translation", translation);
    }

    public JSONObject getDict() {
        return dict;
    }
}
