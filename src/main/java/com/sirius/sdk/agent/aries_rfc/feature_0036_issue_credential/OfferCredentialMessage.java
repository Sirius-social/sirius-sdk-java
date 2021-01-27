package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.google.gson.JsonObject;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public class OfferCredentialMessage extends BaseIssueCredentialMessage {

    //String comment;

    public OfferCredentialMessage(String message) {
        super(message);
    }

    public String getComment() {
        return (String) this.getMessageObj().get("comment");
    }

    public void setComment(String comment) {
        this.getMessageObj().put("comment", comment);
    }

    public static OfferCredentialMessage create(String comment, String locate, String offer, JsonObject credDef,
                                                List<ProposedAttrib> preview, JSONObject issuerSchema,
                                                List<AttribTranslation> translation, Date expiresTime) {
        JSONObject ofCredObject = new JSONObject();
        ofCredObject.put("@id", generateId());
        ofCredObject.put("@type", ARIES_DOC_URI + "issue-credential/1.0/offer-credential");

        return new OfferCredentialMessage(ofCredObject.toString());
    }
}
