package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof;


import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;


public class PresentationMessage extends BasePresentProofMessage {

    public JSONObject proof() {
        Object obj = getMessageObj().get("presentations~attach");
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

    public static PresentationMessage.Builder<?> builder() {
        return new PresentationMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends BasePresentProofMessage.Builder<B> {
        JSONObject proof = null;
        String presentationId = null;


        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (proof != null) {
                presentationId = presentationId != null ? presentationId : UUID.randomUUID().toString();
                JSONObject attach = new JSONObject();
                attach.put("@id", "libindy-presentation-" + presentationId);
                attach.put("mime-type", "application/json");
                JSONObject data = new JSONObject();
                byte[] base64 = Base64.getEncoder().encode(proof.toString().getBytes(StandardCharsets.UTF_8));
                data.put("base64", new String(base64));
                attach.put("data", data);
                JSONArray arr = new JSONArray();
                arr.put(attach);
                jsonObject.put("presentations~attach", arr);

            }

            return jsonObject;
        }
    }

    private static class PresentationMessageBuilder extends PresentationMessage.Builder<PresentationMessageBuilder> {
        @Override
        protected PresentationMessageBuilder self() {
            return this;
        }
    }


}
