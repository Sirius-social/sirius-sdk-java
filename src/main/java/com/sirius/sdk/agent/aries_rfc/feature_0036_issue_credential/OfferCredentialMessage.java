package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class OfferCredentialMessage extends BaseIssueCredentialMessage {
    public static class ParseResult {
        JSONObject offer = null;
        JSONObject offerBody = null;
        JSONObject credDefBody = null;
    }

    JSONObject offer = null;
    JSONObject credDef = null;

    public OfferCredentialMessage(String message) {
        super(message);
    }

    public OfferCredentialMessage() {
        super("{}");
        this.getMessageObj().put("@id", generateId());
        this.getMessageObj().put("@type", ARIES_DOC_URI + "issue-credential/1.0/offer-credential");
    }

    public String getComment() {
        return this.getMessageObj().getString("comment");
    }

    public OfferCredentialMessage setComment(String comment) {
        this.getMessageObj().put("comment", comment);
        return this;
    }

    private void fillOffersAttach() {
        if (offer != null && credDef != null) {
            JSONObject payload = new JSONObject();
            for (String key : JSONObject.getNames(offer))
                payload.put(key, offer.get(key));
            for (String key : JSONObject.getNames(credDef))
                payload.put(key, credDef.get(key));

            JSONObject offersAttach = new JSONObject();
            offersAttach.put("@id", "libindy-cred-offer-" + getId());
            offersAttach.put("mime-type", "application/json");
            JSONObject data = new JSONObject();
            byte[] base64 = Base64.getEncoder().encode(payload.toString().getBytes(StandardCharsets.UTF_8));
            data.put("base64", new String(base64));
            offersAttach.put("data", data);
            this.getMessageObj().put("offers~attach", offersAttach);
        }
    }

    public OfferCredentialMessage setOffer(JSONObject offer) {
        this.offer = offer;
        fillOffersAttach();
        return this;
    }

    public OfferCredentialMessage setCredDef(JSONObject credDef) {
        this.credDef = credDef;
        fillOffersAttach();
        return this;
    }

    public OfferCredentialMessage setPreview(List<ProposedAttrib> preview) {
        JSONObject credPreview = new JSONObject();
        credPreview.put("@type", BaseIssueCredentialMessage.CREDENTIAL_PREVIEW_TYPE);
        JSONArray attributes = new JSONArray();
        for (ProposedAttrib attrib : preview)
            attributes.put(attrib.getDict());
        credPreview.put("attributes", attributes);
        this.getMessageObj().put("credential_preview", credPreview);

        return this;
    }

    public OfferCredentialMessage setIssuerSchema(JSONObject issuerSchema) {
        if (this.getMessageObj().has("~attach"))
            this.getMessageObj().remove("~attach");

        JSONObject attach = new JSONObject();
        attach.put("@type", BaseIssueCredentialMessage.ISSUER_SCHEMA_TYPE);
        attach.put("id", BaseIssueCredentialMessage.ISSUER_SCHEMA_ID);
        attach.put("mime-type", "application/json");
        JSONObject data = new JSONObject();
        data.put("json", issuerSchema);
        attach.put("data", data);
        JSONArray attaches = new JSONArray();
        attaches.put(attach);
        this.getMessageObj().put("~attach", attaches);

        return this;
    }

    public OfferCredentialMessage setTranslation(List<AttribTranslation> translation) {
        if (this.getMessageObj().has("~attach"))
            this.getMessageObj().remove("~attach");

        JSONObject attach = new JSONObject();
        attach.put("@type", BaseIssueCredentialMessage.CREDENTIAL_PREVIEW_TYPE);
        attach.put("id", BaseIssueCredentialMessage.CREDENTIAL_TRANSLATION_ID);
        JSONObject l10n = new JSONObject();
        l10n.put("locale", this.locale);
        attach.put("~l10n", l10n);
        attach.put("mime-type", "application/json");
        JSONObject data = new JSONObject();
        JSONArray transArr = new JSONArray();
        for (AttribTranslation trans : translation) {
            transArr.put(trans.getDict());
        }
        data.put("json", transArr);
        attach.put("data", data);
        JSONArray attaches = new JSONArray();
        attaches.put(attach);
        this.getMessageObj().put("~attach", attaches);

        return this;
    }

    public OfferCredentialMessage expiresTime(Date expiresTime) {
        JSONObject timing = new JSONObject();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd THH:mm:ss");
        timing.put("expires_time", df.format(expiresTime));
        this.getMessageObj().put("~timing", timing);

        return this;
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
}
