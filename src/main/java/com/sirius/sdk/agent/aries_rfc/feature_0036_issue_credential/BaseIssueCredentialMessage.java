package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONObject;

public class BaseIssueCredentialMessage extends AriesProtocolMessage {
    public static final String PROTOCOL = "issue-credential";

    @Override
    public String getProtocol() {
        return "issue-credential";
    }

    @Override
    public String getName() {
        return null;
    }

    public String ackMessageId() {
        JSONObject pleaseAck = getJSONOBJECTFromJSON("~please_ack", "{}");
        if (pleaseAck.has("message_id")) {
            return pleaseAck.getString("message_id");
        }
        return this.getId();
    }

    public BaseIssueCredentialMessage(String message) {
        super(message);
    }
}
