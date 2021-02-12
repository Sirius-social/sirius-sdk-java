package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONObject;

public class BaseIssueCredentialMessage extends AriesProtocolMessage {
    public static final String PROTOCOL = "issue-credential";
    public static final String DEF_LOCALE = "en";
    public static final String CREDENTIAL_PREVIEW_TYPE = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/issue-credential/1.0/credential-preview";
    public static final String CREDENTIAL_TRANSLATION_TYPE = "https://github.com/Sirius-social/agent/tree/master/messages/credential-translation";
    public static final String ISSUER_SCHEMA_TYPE = "https://github.com/Sirius-social/agent/tree/master/messages/issuer-schema";
    public static final String CREDENTIAL_TRANSLATION_ID = "credential-translation";
    public static final String ISSUER_SCHEMA_ID = "issuer-schema";

    @Override
    public String getProtocol() {
        return "issue-credential";
    }

    @Override
    public String getName() {
        return null;
    }

    public String ackMessageId() {
        JSONObject pleaseAck = getJSONOBJECTFromJSON("~please_ack", "{}");
        if (pleaseAck.has("message_id")) {
            return pleaseAck.getString("message_id");
        }
        return this.getId();
    }

    public BaseIssueCredentialMessage(String message) {
        super(message);
    }

    public static abstract class Builder<B extends Builder<B>> {
        protected String locale = DEF_LOCALE;

        public B setLocale(String locate) {
            this.locale = locate;
            return self();
        }

        protected abstract B self();

        protected Builder() {}

        protected JSONObject generateJSON() {
            JSONObject jsonObject = new JSONObject();
            JSONObject l10n = new JSONObject();
            l10n.put("locale", locale);
            jsonObject.put("~l10n", l10n);
            return jsonObject;
        }
    }
}
