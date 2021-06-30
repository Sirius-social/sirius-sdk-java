package com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import com.sirius.sdk.agent.aries_rfc.concept_0017_attachments.Attach;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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

    public List<Attach> getAttaches() {
        List<Attach> res = new ArrayList<>();
        if (messageObjectHasKey("~attach")) {
            JSONArray arr = getMessageObj().getJSONArray("~attach");
            for (Object o : arr) {
                res.add(new Attach((JSONObject) o));
            }
        }
        return res;
    }

    public void addAttach(Attach att) {
        if (!messageObjectHasKey("~attach")) {
            getMessageObj().put("~attach", new JSONArray());
        }
        getMessageObj().getJSONArray("~attach").put(att);
    }

    public static abstract class Builder<B extends Message.Builder<B>> extends AriesProtocolMessage.Builder<B> {
        String locale = null;
        String content = null;

        public B setLocale(String locale) {
            this.locale = locale;
            return self();
        }

        public B setContent(String content) {
            this.content = content;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            String id = jsonObject.optString("id");

            if (locale != null) {
                jsonObject.put("~l10n", (new JSONObject().
                        put("locale", locale)));
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
