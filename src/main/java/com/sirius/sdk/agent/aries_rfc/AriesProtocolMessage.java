package com.sirius.sdk.agent.aries_rfc;

import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.messaging.Type;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

public abstract class AriesProtocolMessage extends Message {

    public static final String ARIES_DOC_URI = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/";
    public static final String THREAD_DECORATOR = "~thread";

    public AriesProtocolMessage(String message) {
        super(message);
    }

    public AriesProtocolMessage() {
        super("{}");
    }

    public static abstract class Builder<B extends Builder<B>> {

        protected abstract B self();

        protected Builder() {}

        protected JSONObject generateJSON() {
            JSONObject jsonObject = new JSONObject();

            Pair<String, String> protocolAndName = Message.getProtocolAndName((Class<? extends Message>) this.getClass().getDeclaringClass());
            jsonObject.put("@type", (new Type("ARIES_DOC_URI", protocolAndName.first, "1.0", protocolAndName.second)));

            return jsonObject;
        }

    }


}
