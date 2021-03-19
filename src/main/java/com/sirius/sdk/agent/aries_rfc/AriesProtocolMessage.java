package com.sirius.sdk.agent.aries_rfc;

import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.messaging.Type;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

public abstract class AriesProtocolMessage extends Message {

    public static final String ARIES_DOC_URI = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/";
    public static final String THREAD_DECORATOR = "~thread";
    public static final String DEF_VERSION = "1.0";

    public AriesProtocolMessage(String message) {
        super(message);
    }

    public AriesProtocolMessage() {
        super("{}");
    }

    public void validate() throws SiriusValidationError {}

    public String getAckMessageId() {
        JSONObject pleaseAck = getJSONOBJECTFromJSON("~please_ack", "{}");
        if (pleaseAck.has("message_id")) {
            return pleaseAck.getString("message_id");
        }
        return this.getId();
    }

    public boolean hasPleaseAck() {
        return getMessageObj().has("~please_ack");
    }

    public void setPleaseAck(boolean flag) {
        if (flag) {
            JSONObject pleaseAck = new JSONObject();
            pleaseAck.put("message_id", this.getId());
            getMessageObj().put("~please_ack", pleaseAck);
        } else {
            getMessageObj().remove("~please_ack");
        }
    }

    public String getThreadId() {
        if (getMessageObj().has(THREAD_DECORATOR) && getMessageObj().getJSONObject(THREAD_DECORATOR).has("thid")) {
            return getMessageObj().getJSONObject(THREAD_DECORATOR).getString("thid");
        }
        return null;
    }

    public void setThreadId(String thid) {
        JSONObject thread;
        if (getMessageObj().has(THREAD_DECORATOR)) {
            thread = getMessageObj().getJSONObject(THREAD_DECORATOR);
        } else {
            thread = new JSONObject();
        }
        thread.put("thid", thid);
        getMessageObj().put(THREAD_DECORATOR, thread);
    }

    public static abstract class Builder<B extends Builder<B>> {
        String version = DEF_VERSION;
        String docUri = ARIES_DOC_URI;
        String id = null;

        public B setVersion(String version) {
            this.version = version;
            return self();
        }

        public B setDocUri(String docUri) {
            this.docUri = docUri;
            return self();
        }

        public B setId(String id) {
            this.id = id;
            return self();
        }

        protected abstract B self();

        protected Builder() {}

        protected JSONObject generateJSON() {
            JSONObject jsonObject = new JSONObject();

            Pair<String, String> protocolAndName = Message.getProtocolAndName((Class<? extends Message>) this.getClass().getDeclaringClass());
            jsonObject.put("@type", (new Type(docUri, protocolAndName.first, version, protocolAndName.second)));

            jsonObject.put("@id", this.id == null ? generateId() : this.id);

            return jsonObject;
        }

    }


}
