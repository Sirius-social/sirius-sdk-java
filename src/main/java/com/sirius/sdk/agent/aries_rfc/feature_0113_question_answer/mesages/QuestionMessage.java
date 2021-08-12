package com.sirius.sdk.agent.aries_rfc.feature_0113_question_answer.mesages;

import com.google.gson.annotations.SerializedName;
import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuestionMessage extends AriesProtocolMessage {

    static {
        com.sirius.sdk.messaging.Message.registerMessageClass(QuestionMessage.class, "questionanswer", "question");
    }

    public QuestionMessage(String msg) {
        super(msg);
    }

    public static Builder<?> builder() {
        return new MessageBuilder();
    }

    public String getQuestionText() {
        return getMessageObj().optString("question_text");
    }

    public String getQuestionDetail() {
        return getMessageObj().optString("question_detail");
    }

    public String getNonce() {
        return getMessageObj().optString("nonce");
    }

    public boolean isSignatureRequired() {
        return getMessageObj().optBoolean("signature_required", false);
    }

    public List<String> getValidResponses() {
        List<String> responsesLis = new ArrayList<>();
        JSONArray responses = getMessageObj().optJSONArray("valid_responses");
        if (responses != null) {
            for (int i = 0; i < responses.length(); i++) {
                JSONObject response = responses.optJSONObject(i);
                if (response != null) {
                    String validResponse = response.optString("text");
                    responsesLis.add(validResponse);
                }
            }
        }
        return responsesLis;
    }

    public String getContent() {
        return getMessageObj().optString("content");
    }

    public ZonedDateTime getExpiresTime() {
        JSONObject timing = getMessageObj().optJSONObject("~timing");
        if (timing != null) {
            String expiresTimeStr = timing.optString("expires_time");
            if (!expiresTimeStr.isEmpty())
                return ZonedDateTime.parse(expiresTimeStr);
        }
        return null;
    }

    public static abstract class Builder<B extends QuestionMessage.Builder<B>> extends AriesProtocolMessage.Builder<B> {
        String questionText = null;
        String questionDetail = null;
        String nonce = null;
        boolean signatureRequired = false;
        List<String> validResponses = null;
        Date expiresTime = null;
        Integer ttlSec = null;

        public B setQuestionText(String text) {
            this.questionText = text;
            return self();
        }

        public B setQuestionDetail(String questionDetail) {
            this.questionDetail = questionDetail;
            return self();
        }

        public B setExpiresTime(Date expiresTime) {
            this.expiresTime = expiresTime;
            return self();
        }

        public B setTtl(int seconds) {
            this.ttlSec = seconds;
            return self();
        }

        public B setNonce(String nonce) {
            this.nonce = nonce;
            return self();
        }

        public B setSignatureRequired(boolean signatureRequired) {
            this.signatureRequired = signatureRequired;
            return self();
        }

        public B setValidResponses(List<String> validResponses) {
            this.validResponses = validResponses;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            String id = jsonObject.optString("id");

            if (questionText != null) {
                jsonObject.put("question_text", questionText);
            }

            if (questionDetail != null) {
                jsonObject.put("question_detail", questionDetail);
            }
            if (nonce != null) {
                jsonObject.put("nonce", nonce);
            }

            jsonObject.put("signature_required", signatureRequired);

            if (ttlSec != null) {
                JSONObject timing = jsonObject.optJSONObject("~timing");
                if (timing == null) {
                    timing = new JSONObject();
                    jsonObject.put("~timing", timing);
                }
                String expiresTimeIso = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(ttlSec).format(DateTimeFormatter.ISO_INSTANT);
                timing.put("expires_time", expiresTimeIso);
            }

            if (validResponses != null) {
                JSONArray validArray = new JSONArray(validResponses);
                for (int i = 0; i < validResponses.size(); i++) {
                    JSONObject responseObj = new JSONObject();
                    responseObj.put("text", validResponses.get(i));
                    validArray.put(responseObj);
                }
                jsonObject.put("valid_responses", validArray);
            }

            return jsonObject;
        }

        public QuestionMessage build() {
            return new QuestionMessage(generateJSON().toString());
        }
    }

    private static class MessageBuilder extends Builder<MessageBuilder> {
        @Override
        protected QuestionMessage.MessageBuilder self() {
            return this;
        }
    }
}
