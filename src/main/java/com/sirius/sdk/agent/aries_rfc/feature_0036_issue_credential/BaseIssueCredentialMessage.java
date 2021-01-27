package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;

public class BaseIssueCredentialMessage extends AriesProtocolMessage {
    @Override
    public String getProtocol() {
        return "issue-credential";
    }

    @Override
    public String getName() {
        return null;
    }

    public BaseIssueCredentialMessage(String message) {
        super(message);
    }
}
