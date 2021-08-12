package com.sirius.sdk.agent.aries_rfc.feature_0113_question_answer.mesages;

import com.google.gson.annotations.SerializedName;
import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuestionMessage extends AriesProtocolMessage {

    static {
        com.sirius.sdk.messaging.Message.registerMessageClass(QuestionMessage.class, "questionanswer", "question");
    }

    /* "question_text": "Alice, are you on the phone with Bob from Faber Bank right now?",
         "question_detail": "This is optional fine-print giving context to the question and its various answers.",
         "nonce": "<valid_nonce>",
         "signature_required": true,
         "valid_responses" : [
    {"text": "Yes, it's me"},
    {"text": "No, that's not me!"}],
            "~timing": {
        "expires_time": "2018-12-13T17:29:06+0000"
    }*/

    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

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

    public Date expiresTime() {
        JSONObject timing = getMessageObj().optJSONObject("~timing");
        if (timing != null) {
            String dateTimeStr = timing.optString("expires_time", "");
            if (!dateTimeStr.isEmpty()) {
                DateFormat df = new SimpleDateFormat(TIME_FORMAT);
                try {
                    return df.parse(dateTimeStr);
                } catch (ParseException ignored) {
                }
            }
        }
        return null;
    }

    public QuestionMessage(String msg) {
        super(msg);
    }

    public String getContent() {
        return getMessageObj().optString("content");
    }

    public static Builder<?> builder() {
        return new MessageBuilder();
    }


    public static abstract class Builder<B extends QuestionMessage.Builder<B>> extends AriesProtocolMessage.Builder<B> {
        String questionText = null;
        String questionDetail = null;
        String nonce = null;
        boolean signatureRequired = false;
        List<String> validResponses = null;
        Date expiresTime = null;

        public B setQuestionText(String locale) {
            this.questionText = locale;
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

            if (validResponses != null) {
                JSONArray validArray =  new JSONArray(validResponses);
                for(int i=0;i<validResponses.size();i++){
                    JSONObject responseObj =  new JSONObject();
                    responseObj.put("text",validResponses.get(i));
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
