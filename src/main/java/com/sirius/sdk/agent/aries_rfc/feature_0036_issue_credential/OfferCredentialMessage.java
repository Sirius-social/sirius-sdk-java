package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.google.gson.JsonObject;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class OfferCredentialMessage extends BaseIssueCredentialMessage {

    public static class ParseResult {
        JSONObject offer = null;
        JSONObject offerBody = null;
        JSONObject credDefBody = null;
    }

    public OfferCredentialMessage(String message) {
        super(message);
    }

    public String getComment() {
        return (String) this.getMessageObj().get("comment");
    }

    public void setComment(String comment) {
        this.getMessageObj().put("comment", comment);
    }

    public ParseResult parse() throws SiriusValidationError {
        JSONArray offerAttaches = getMessageObj().getJSONArray("offers~attach");
        if (offerAttaches == null) {
            JSONObject att = getMessageObj().getJSONObject("offers~attach");
            if (att != null) {
                offerAttaches = new JSONArray();
                offerAttaches.put(att);
            }
        }

        if (offerAttaches == null) {
            throw new SiriusValidationError("Expected attribute \"offer~attach\" must contains cred-Offer and cred-Def");
        }

        ParseResult res = new ParseResult();
        res.offer = offerAttaches.getJSONObject(0);

        for (int i = 0; i < offerAttaches.length(); i++) {
            JSONObject attach = offerAttaches.getJSONObject(i);
            if (attach.has("data") && attach.getJSONObject("data").has("base64")) {
                String rawBase64 = attach.getJSONObject("data").getString("base64");
                JSONObject payload = new JSONObject(Base64.getDecoder().decode(rawBase64));
                Set<String> offerFields = new HashSet<>(Arrays.asList("key_correctness_proof", "nonce", "schema_id", "cred_def_id"));
                Set<String> credDefFields = new HashSet<>(Arrays.asList("value", "type", "ver", "schemaId", "id", "tag"));
                if (payload.keySet().containsAll(offerFields)) {
                    res.offerBody = new JSONObject();
                    for (String field : offerFields) {
                        res.offerBody.put(field, payload.getString(field));
                    }
                }
                if (payload.keySet().containsAll(credDefFields)) {
                    res.credDefBody = new JSONObject();
                    for (String field : credDefFields) {
                        res.credDefBody.put(field, payload.getString(field));
                    }
                }
            }
        }

        if (res.offerBody == null) {
            throw new SiriusValidationError("Expected offer~attach must contains Payload with offer");
        }

        if (res.credDefBody == null) {
            throw new SiriusValidationError("Expected offer~attach must contains Payload with cred_def data");
        }

        return res;
    }

    public JSONObject offer() throws SiriusValidationError {
        return parse().offerBody;
    }

    public JSONObject credDef() throws SiriusValidationError {
        return parse().credDefBody;
    }

    public static OfferCredentialMessage create(String comment, String locate, String offer, JsonObject credDef,
                                                List<ProposedAttrib> preview, JSONObject issuerSchema,
                                                List<AttribTranslation> translation, Date expiresTime) {
        JSONObject ofCredObject = new JSONObject();
        ofCredObject.put("@id", generateId());
        ofCredObject.put("@type", ARIES_DOC_URI + "issue-credential/1.0/offer-credential");
        ofCredObject.put("comment", comment);
        ofCredObject.put("locate", locate);
        ofCredObject.put("offer", offer);
        ofCredObject.put("cred_def", credDef);
        ofCredObject.put("preview", preview);
        ofCredObject.put("issuer_schema", issuerSchema);
        ofCredObject.put("translation", translation);
        ofCredObject.put("expires_time", expiresTime);

        return new OfferCredentialMessage(ofCredObject.toString());
    }
}
