package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages;

import org.json.JSONObject;

public class ProposedAttrib extends JSONObject {

    public ProposedAttrib() {
        super();
    }

    public ProposedAttrib(JSONObject o) {
        super(o.toString());
    }

    public ProposedAttrib(String name, String value) {
        put("name", name);
        put("value", value);
    }

    public ProposedAttrib(String name, String value, String mimeType) {
        put("name", name);
        put("value", value);
        put("mime-type", mimeType);
    }


    public String getName(){
        return optString("name");
    }

    public String getValue(){
        return optString("value");
    }

    public String getMimeType(){
        return optString("mime-type");
    }
}
