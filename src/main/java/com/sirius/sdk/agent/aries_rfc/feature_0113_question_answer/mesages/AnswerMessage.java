package com.sirius.sdk.agent.aries_rfc.feature_0113_question_answer.mesages;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONObject;

public class AnswerMessage extends AriesProtocolMessage {

    static {
        com.sirius.sdk.messaging.Message.registerMessageClass(AnswerMessage.class, "questionanswer", "answer");
    }

    public AnswerMessage(String msg) {
        super(msg);
    }

    public String getResponse() {
        return getMessageObj().optString("response");
    }

//    public void setOutTime() {
//        JSONObject timing = getMessageObj().optJSONObject("~timing");
//        if (timing == null) {
//            timing = new JSONObject();
//            getMessageObj().put("~timing", timing);
//        }
//    }

    public static Builder<?> builder() {
        return new MessageBuilder();
    }

    public static abstract class Builder<B extends AnswerMessage.Builder<B>> extends AriesProtocolMessage.Builder<B> {
        String response = null;

        public B setResponse(String response) {
            this.response = response;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (response != null) {
                jsonObject.put("response", response);
            }

            return jsonObject;
        }

        public AnswerMessage build() {
            return new AnswerMessage(generateJSON().toString());
        }
    }

    private static class MessageBuilder extends Builder<MessageBuilder> {
        @Override
        protected AnswerMessage.MessageBuilder self() {
            return this;
        }
    }
}
