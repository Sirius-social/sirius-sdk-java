package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.messaging.Type;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Base64;

public class RequestCredentialMessage extends BaseIssueCredentialMessage{
    public RequestCredentialMessage(String message) {
        super(message);
    }

    public JSONObject credRequest() {
        JSONObject request = this.getJSONOBJECTFromJSON("requests~attach");
        if (request == null) {
            JSONArray arr = this.getJSONArrayFromJSON("requests~attach", new JSONArray());
            if (arr.length() > 0) {
                request = arr.getJSONObject(0);
            }
        }

        if (request !=null) {
            String base64 = request.getJSONObject("data").getString("base64");
            byte[] decoded = Base64.getDecoder().decode(base64);
            return new JSONObject(decoded);
        }
        return new JSONObject("{}");
    }

    public static RequestCredentialMessage create(String comment, String locate, String credRequest, String docUri) {
        JSONObject reqCredMsgJson = new JSONObject();
        reqCredMsgJson.put("@id", generateId());
        reqCredMsgJson.put("@type", ARIES_DOC_URI + "issue-credential/1.0/request-credential");
        reqCredMsgJson.put("comment", comment);
        reqCredMsgJson.put("locate", locate);
        reqCredMsgJson.put("cred_request", credRequest);
        reqCredMsgJson.put("doc_uri", docUri);
        return new RequestCredentialMessage(reqCredMsgJson.toString());
    }
}
