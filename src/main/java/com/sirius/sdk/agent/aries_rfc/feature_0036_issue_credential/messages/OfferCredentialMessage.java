package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages;

import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class OfferCredentialMessage extends BaseIssueCredentialMessage {

    static {
        Message.registerMessageClass(OfferCredentialMessage.class, BaseIssueCredentialMessage.PROTOCOL, "offer-credential");
    }

    public static class ParseResult {
        JSONObject offer = null;
        JSONObject offerBody = null;
        JSONObject credDefBody = null;
    }

    public OfferCredentialMessage(String message) {
        super(message);
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
                JSONObject payload = new JSONObject(new String(Base64.getDecoder().decode(rawBase64)));
                Set<String> offerFields = new HashSet<>(Arrays.asList("key_correctness_proof", "nonce", "schema_id", "cred_def_id"));
                Set<String> credDefFields = new HashSet<>(Arrays.asList("value", "type", "ver", "schemaId", "id", "tag"));
                if (payload.keySet().containsAll(offerFields)) {
                    res.offerBody = new JSONObject();
                    for (String field : offerFields) {
                        res.offerBody.put(field, payload.get(field));
                    }
                }
                if (payload.keySet().containsAll(credDefFields)) {
                    res.credDefBody = new JSONObject();
                    for (String field : credDefFields) {
                        res.credDefBody.put(field, payload.get(field));
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

    public List<ProposedAttrib> getCredentialPreview() {
        List<ProposedAttrib> res = new ArrayList<>();
        JSONObject credentialPreview = getMessageObj().optJSONObject("credential_preview");
        if (credentialPreview != null) {
            if (credentialPreview.optString("@type").equals(CREDENTIAL_PREVIEW_TYPE)) {
                JSONArray attribs = credentialPreview.optJSONArray("attributes");
                if (attribs != null) {
                    for (Object o : attribs) {
                        res.add(new ProposedAttrib((JSONObject) o));
                    }
                }
            }
        }

        return res;
    }

    public static Builder<?> builder() {
        return new OfferCredentialMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends BaseIssueCredentialMessage.Builder<B> {
        JSONObject offer = null;
        JSONObject credDef = null;
        List<AttribTranslation> translation = null;
        List<ProposedAttrib> preview = null;
        JSONObject issuerSchema = null;
        Date expiresTime = null;

        public B setOffer(JSONObject offer) {
            this.offer = offer;
            return self();
        }

        public B setCredDef(JSONObject credDef) {
            this.credDef = credDef;
            return self();
        }

        public B setIssuerSchema(JSONObject issuerSchema) {
            this.issuerSchema = issuerSchema;
            return self();
        }

        public B setTranslation(List<AttribTranslation> translation) {
            this.translation = translation;
            return self();
        }

        public B setPreview(List<ProposedAttrib> preview) {
            this.preview = preview;
            return self();
        }

        public B setExpiresTime(Date expiresTime) {
            this.expiresTime = expiresTime;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            String id = jsonObject.optString("id");

            if (preview != null && !preview.isEmpty()) {
                JSONObject credPreview = new JSONObject();
                credPreview.put("@type", BaseIssueCredentialMessage.CREDENTIAL_PREVIEW_TYPE);
                JSONArray attributes = new JSONArray();
                for (ProposedAttrib attrib : preview)
                    attributes.put(attrib);
                credPreview.put("attributes", attributes);
                jsonObject.put("credential_preview", credPreview);
            }

            if (offer != null && credDef != null) {
                JSONObject payload = new JSONObject();
                for (String key : JSONObject.getNames(offer))
                    payload.put(key, offer.get(key));
                for (String key : JSONObject.getNames(credDef))
                    payload.put(key, credDef.get(key));

                JSONObject offersAttach = new JSONObject();
                offersAttach.put("@id", "libindy-cred-offer-" + id);
                offersAttach.put("mime-type", "application/json");
                JSONObject data = new JSONObject();
                byte[] base64 = Base64.getEncoder().encode(payload.toString().getBytes(StandardCharsets.UTF_8));
                data.put("base64", new String(base64));
                offersAttach.put("data", data);
                JSONArray attaches = new JSONArray();
                attaches.put(offersAttach);
                jsonObject.put("offers~attach", attaches);
            }

            if (translation != null && !translation.isEmpty()) {
                if (!jsonObject.has("~attach"))
                    jsonObject.put("~attach", new JSONArray());

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

                JSONArray attaches = jsonObject.getJSONArray("~attach");
                attaches.put(attach);
            }

            if (issuerSchema != null) {
                if (!jsonObject.has("~attach"))
                    jsonObject.put("~attach", new JSONArray());

                JSONObject attach = new JSONObject();
                attach.put("@type", BaseIssueCredentialMessage.ISSUER_SCHEMA_TYPE);
                attach.put("id", BaseIssueCredentialMessage.ISSUER_SCHEMA_ID);
                attach.put("mime-type", "application/json");
                JSONObject data = new JSONObject();
                data.put("json", issuerSchema);
                attach.put("data", data);

                JSONArray attaches = jsonObject.getJSONArray("~attach");
                attaches.put(attach);
            }

            if(expiresTime != null) {
                JSONObject timing = new JSONObject();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                timing.put("expires_time", df.format(expiresTime));
                jsonObject.put("~timing", timing);
            }

            return jsonObject;
        }

        public OfferCredentialMessage build() {
            return new OfferCredentialMessage(generateJSON().toString());
        }
    }

    private static class OfferCredentialMessageBuilder extends Builder<OfferCredentialMessageBuilder> {
        @Override
        protected OfferCredentialMessageBuilder self() {
            return this;
        }
    }
}
