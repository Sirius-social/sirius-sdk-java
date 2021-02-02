package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import org.json.JSONObject;

public class IssueCredentialMessage extends BaseIssueCredentialMessage{
    public IssueCredentialMessage(String message) {
        super(message);
    }

    public static IssueCredentialMessage create(String comment, String locate, String cred, String credId) {
        JSONObject issCredMsg = new JSONObject();
        issCredMsg.put("comment", comment);
        issCredMsg.put("locate", locate);
        issCredMsg.put("cred", cred);
        issCredMsg.put("cred_id", credId);
        return new IssueCredentialMessage(issCredMsg.toString());
    }
}
