package com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONObject;

public class Message extends AriesProtocolMessage {

    static {
        com.sirius.sdk.messaging.Message.registerMessageClass(Message.class, "basicmessage", "message");
    }

    public Message(String msg) {
        super(msg);
    }

    public String getContent() {
        return getMessageObj().optString("content");
    }

    public static Builder<?> builder() {
        return new MessageBuilder();
    }

    public static abstract class Builder<B extends Message.Builder<B>> extends AriesProtocolMessage.Builder<B> {
        String locale = null;
        String content = null;

        public B setLocale(String locale) {
            this.locale = locale;
            return self();
        }

        public B setContext(String context) {
            this.content = context;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (locale != null) {
                jsonObject.put("~l10n", (new JSONObject().
                        put(locale, locale)));
            }

            if (content != null) {
                jsonObject.put("content", content);
            }

            return jsonObject;
        }

        public Message build() {
            return new Message(generateJSON().toString());
        }
    }

    private static class MessageBuilder extends Builder<MessageBuilder> {
        @Override
        protected Message.MessageBuilder self() {
            return this;
        }
    }
}
