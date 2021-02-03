package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Base64;

public class IssueCredentialMessage extends BaseIssueCredentialMessage{
    public IssueCredentialMessage(String message) {
        super(message);
    }

    public JSONObject cred() {
        JSONObject attach = getAttach();

        if (attach != null) {
            String b64 = attach.getJSONObject("data").getString("base64");
            return new JSONObject(Base64.getDecoder().decode(b64));
        }

        return null;
    }

    public String credId() {
        return getAttach().getString("@id");
    }

    protected JSONObject getAttach() {
        JSONObject attach = getJSONOBJECTFromJSON("credentials~attach");
        if (attach == null) {
            JSONArray arr = getJSONArrayFromJSON("credentials~attach", new JSONArray());
            if (arr.length() > 0) {
                attach = arr.getJSONObject(0);
            }
        }
        return attach;
    }

    public static IssueCredentialMessage create(String comment, String locate, String cred, String credId) {
        JSONObject issCredMsg = new JSONObject();
        issCredMsg.put("@id", generateId());
        issCredMsg.put("@type", ARIES_DOC_URI + "issue-credential/1.0/issue-credential");
        issCredMsg.put("comment", comment);
        issCredMsg.put("locate", locate);
        issCredMsg.put("cred", cred);
        issCredMsg.put("cred_id", credId);
        return new IssueCredentialMessage(issCredMsg.toString());
    }
}
