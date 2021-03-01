package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages;


import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.AttribTranslation;
import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RequestPresentationMessage extends BasePresentProofMessage {

    static {
        Message.registerMessageClass(RequestPresentationMessage.class, BasePresentProofMessage.PROTOCOL, "request-presentation");
    }

    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public RequestPresentationMessage(String msg) {
        super(msg);
    }

    public static RequestPresentationMessage.Builder<?> builder() {
        return new RequestPresentationMessageBuilder();
    }

    public JSONObject proofRequest() {
        Object obj = getMessageObj().get("request_presentations~attach");
        JSONObject attach = null;
        if (obj instanceof JSONArray && !((JSONArray) obj).isEmpty()) {
            attach = ((JSONArray) obj).getJSONObject(0);
        }
        if (obj instanceof JSONObject) {
            attach = (JSONObject) obj;
        }

        if (attach != null && attach.has("data") && attach.getJSONObject("data").has("base64")) {
            String rawBase64 = attach.getJSONObject("data").getString("base64");
            return new JSONObject(new String(Base64.getDecoder().decode(rawBase64)));
        }

        return null;
    }

    public Date expiresTime() {
        JSONObject timing = getMessageObj().optJSONObject("~timing");
        if (timing != null) {
            String dateTimeStr = timing.optString("expires_time", "");
            if (!dateTimeStr.isEmpty()) {
                DateFormat df = new SimpleDateFormat(TIME_FORMAT);
                try {
                    return df.parse(dateTimeStr);
                } catch (ParseException ignored) {}
            }
        }
        return null;
    }

    public static abstract class Builder<B extends Builder<B>> extends BasePresentProofMessage.Builder<B> {
        JSONObject proofRequest = null;
        List<AttribTranslation> translation = null;
        Date expiresTime = null;

        public B setProofRequest(JSONObject proofRequest) {
            this.proofRequest = proofRequest;
            return self();
        }

        public B setTranslation(List<AttribTranslation> translation) {
            this.translation = translation;
            return self();
        }

        public B setExpiresTime(Date expiresTime) {
            this.expiresTime = expiresTime;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (proofRequest != null) {
                JSONObject requestPresentationsAttach = new JSONObject();
                requestPresentationsAttach.put("@id", "libindy-request-presentation-" + UUID.randomUUID().toString());
                requestPresentationsAttach.put("mime-type", "application/json");
                JSONObject data = new JSONObject();
                byte[] base64 = Base64.getEncoder().encode(proofRequest.toString().getBytes(StandardCharsets.UTF_8));
                data.put("base64", new String(base64));
                requestPresentationsAttach.put("data", data);
                JSONArray arr = new JSONArray();
                arr.put(requestPresentationsAttach);
                jsonObject.put("request_presentations~attach", arr);
            }

            if (translation != null && !translation.isEmpty()) {
                if (jsonObject.has("~attach"))
                    jsonObject.remove("~attach");

                JSONObject attach = new JSONObject();
                attach.put("@type", BasePresentProofMessage.CREDENTIAL_TRANSLATION_TYPE);
                attach.put("id", BasePresentProofMessage.CREDENTIAL_TRANSLATION_ID);
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
                jsonObject.put("~attach", attaches);
            }

            if(expiresTime != null) {
                JSONObject timing = new JSONObject();
                DateFormat df = new SimpleDateFormat(TIME_FORMAT);
                timing.put("expires_time", df.format(expiresTime));
                jsonObject.put("~timing", timing);
            }

            return jsonObject;
        }

        public RequestPresentationMessage build() {
            return new RequestPresentationMessage(generateJSON().toString());
        }

    }

    private static class RequestPresentationMessageBuilder extends RequestPresentationMessage.Builder<RequestPresentationMessageBuilder> {
        @Override
        protected RequestPresentationMessageBuilder self() {
            return this;
        }
    }


}
