package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import org.json.JSONArray;
import org.json.JSONObject;

public class RequestCredentialMessage extends BaseIssueCredentialMessage{
    public RequestCredentialMessage(String message) {
        super(message);
    }

    public String credRequest() {
//        JSONObject request = this.getJSONOBJECTFromJSON("requests~attach");
//        if (request == null) {
//            JSONArray arr = this.getJSONArrayFromJSON("requests~attach");
//        }
//
//        if (!request.isEmpty()) {
//            if isinstance(request, list):
//            request = request[0]
//            body = request.get('data').get('base64')
//            body = base64.b64decode(body)
//            body = json.loads(body.decode())
//            return body
//        } else
            return "";
    }
}
