package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

public class OfferCredentialMessage extends BaseIssueCredentialMessage {

    //String comment;

    public OfferCredentialMessage() {

    }

    public String getComment() {
        return (String) this.getMessageObj().get("comment");
    }

    public void setComment(String comment) {
        this.getMessageObj().put("comment", comment);
    }
}
