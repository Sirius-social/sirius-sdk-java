package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONObject;

public class BasePresentProofMessage extends AriesProtocolMessage {
    public static final String DEF_LOCALE = "en";
    public static final String CREDENTIAL_TRANSLATION_TYPE = "https://github.com/Sirius-social/agent/tree/master/messages/credential-translation";
    public static final String CREDENTIAL_TRANSLATION_ID = "credential-translation";


    @Override
    public String getProtocol() {
        return "present-proof";
    }

    @Override
    public String getName() {
        return null;
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProtocolMessage.Builder<B> {
        protected String locale = DEF_LOCALE;
        String comment = null;

        public B setLocale(String locate) {
            this.locale = locate;
            return self();
        }

        public B setComment(String comment) {
            this.comment = comment;
            return self();
        }

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            JSONObject l10n = new JSONObject();
            l10n.put("locale", locale);
            jsonObject.put("~l10n", l10n);

            if (comment != null) {
                jsonObject.put("comment", comment);
            }

            return jsonObject;
        }

    }
}
