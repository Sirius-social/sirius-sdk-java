package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class IssueCredentialMessage extends BaseIssueCredentialMessage {

    static {
        Message.registerMessageClass(IssueCredentialMessage.class, "issue-credential", "issue-credential");
    }

    public IssueCredentialMessage(String message) {
        super(message);
    }

    public JSONObject cred() {
        JSONObject attach = getAttach();

        if (attach != null) {
            String b64 = attach.getJSONObject("data").getString("base64");
            return new JSONObject(new String(Base64.getDecoder().decode(b64)));
        }

        return null;
    }

    public String credId() {
        return getAttach().getString("@id");
    }

    protected JSONObject getAttach() {
        JSONObject attach = getJSONOBJECTFromJSON("credentials~attach");
        if (attach == null) {
            JSONArray arr = getJSONArrayFromJSON("credentials~attach", new JSONArray());
            if (arr.length() > 0) {
                attach = arr.getJSONObject(0);
            }
        }
        return attach;
    }

    public static IssueCredentialMessage.Builder<?> builder() {
        return new IssueCredentialMessage.IssueCredentialMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends BaseIssueCredentialMessage.Builder<B> {
        JSONObject cred = null;
        String credId = null;

        public B setCred(JSONObject cred) {
            this.cred = cred;
            return self();
        }

        public B setCredId(String credId) {
            this.credId = credId;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            String id = generateId();
            jsonObject.put("@id", id);

            if (cred != null) {
                String messageId = credId != null ? credId : "libindy-cred-" + id;
                JSONObject credAttach = new JSONObject();
                credAttach.put("@id", messageId);
                credAttach.put("mime-type", "application/json");
                JSONObject data = new JSONObject();
                byte[] base64 = Base64.getEncoder().encode(cred.toString().getBytes(StandardCharsets.UTF_8));
                data.put("base64", new String(base64));
                credAttach.put("data", data);
                JSONArray attaches = new JSONArray();
                attaches.put(credAttach);
                jsonObject.put("credentials~attach", attaches);

            }

            return jsonObject;
        }

        public IssueCredentialMessage build() {
            return new IssueCredentialMessage(generateJSON().toString());
        }
    }

    private static class IssueCredentialMessageBuilder extends IssueCredentialMessage.Builder<IssueCredentialMessageBuilder> {
        @Override
        protected IssueCredentialMessageBuilder self() {
            return this;
        }
    }

}
