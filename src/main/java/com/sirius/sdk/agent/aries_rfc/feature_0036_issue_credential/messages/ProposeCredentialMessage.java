package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProposeCredentialMessage  extends BaseIssueCredentialMessage {

    static {
        Message.registerMessageClass(ProposeCredentialMessage.class, BaseIssueCredentialMessage.PROTOCOL, "propose-credential");
    }

    public ProposeCredentialMessage(String message) {
        super(message);
    }

    public String getSchemaIssuerDid() {
        return getMessageObj().optString("schema_issuer_did");
    }

    public String getSchemaId() {
        return getMessageObj().optString("schema_id");
    }

    public String getSchemaName() {
        return getMessageObj().optString("schema_name");
    }

    public String getSchemaVersion() {
        return getMessageObj().optString("schema_version");
    }

    public String getCredDefId() {
        return getMessageObj().optString("cred_def_id");
    }

    public String getIssuerDid() {
        return getMessageObj().optString("issuer_did");
    }

    public List<ProposedAttrib> getCredentialProposal() {
        List<ProposedAttrib> res = new ArrayList<>();
        JSONObject credentialProposal = getMessageObj().optJSONObject("credential_proposal");
        if (credentialProposal != null) {
            if (credentialProposal.optString("@type").equals(CREDENTIAL_PREVIEW_TYPE)) {
                JSONArray attribs = credentialProposal.optJSONArray("attributes");
                if (attribs != null) {
                    for (Object o : attribs) {
                        res.add(new ProposedAttrib((JSONObject) o));
                    }
                }
            }
        }

        return res;
    }

    public static ProposeCredentialMessage.Builder<?> builder() {
        return new ProposeCredentialMessage.ProposeCredentialMessageBuilder();
    }

    public static abstract class Builder<B extends ProposeCredentialMessage.Builder<B>> extends BaseIssueCredentialMessage.Builder<B> {
        List<ProposedAttrib> credentialProposal = null;
        String schemaIssuerDid = null;
        String schemaId = null;
        String schemaName = null;
        String schemaVersion = null;
        String credDefId = null;
        String issuerDid = null;

        public B setCredentialProposal(List<ProposedAttrib> credentialProposal) {
            this.credentialProposal = credentialProposal;
            return self();
        }

        public B setSchemaIssuerDid(String schemaIssuerDid) {
            this.schemaIssuerDid = schemaIssuerDid;
            return self();
        }

        public B setSchemaId(String schemaId) {
            this.schemaId = schemaId;
            return self();
        }

        public B setSchemaName(String schemaName) {
            this.schemaName = schemaName;
            return self();
        }

        public B setSchemaVersion(String schemaVersion) {
            this.schemaVersion = schemaVersion;
            return self();
        }

        public B setCredDefId(String credDefId) {
            this.credDefId = credDefId;
            return self();
        }

        public B setIssuerDid(String issuerDid) {
            this.issuerDid = issuerDid;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (credentialProposal != null) {
                JSONObject credProposal = new JSONObject().
                        put("@type", CREDENTIAL_PREVIEW_TYPE);
                JSONArray attributes = new JSONArray();
                for (ProposedAttrib attrib : credentialProposal)
                    attributes.put(attrib);
                credProposal.put("attributes", attributes);
                jsonObject.put("credential_proposal", credProposal);
            }

            if (schemaIssuerDid != null) {
                jsonObject.put("schema_issuer_did", schemaIssuerDid);
            }

            if (schemaId != null) {
                jsonObject.put("schema_id", schemaId);
            }

            if (schemaName != null) {
                jsonObject.put("schema_name", schemaName);
            }

            if (schemaVersion != null) {
                jsonObject.put("schema_version", schemaVersion);
            }

            if (credDefId != null) {
                jsonObject.put("cred_def_id", credDefId);
            }

            if (issuerDid != null) {
                jsonObject.put("issuer_did", issuerDid);
            }

            return jsonObject;
        }

        public ProposeCredentialMessage build() {
            return new ProposeCredentialMessage(generateJSON().toString());
        }
    }

    private static class ProposeCredentialMessageBuilder extends ProposeCredentialMessage.Builder<ProposeCredentialMessage.ProposeCredentialMessageBuilder> {
        @Override
        protected ProposeCredentialMessage.ProposeCredentialMessageBuilder self() {
            return this;
        }
    }
    
    
}
