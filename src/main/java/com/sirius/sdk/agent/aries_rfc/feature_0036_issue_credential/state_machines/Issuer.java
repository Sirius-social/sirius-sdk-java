package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines;

import com.sirius.sdk.agent.Codec;
import com.sirius.sdk.errors.StateMachineTerminatedWithError;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.*;
import com.sirius.sdk.agent.ledger.CredentialDefinition;
import com.sirius.sdk.agent.ledger.Schema;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2P;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Issuer extends BaseIssuingStateMachine {
    Logger log = Logger.getLogger(Issuer.class.getName());

    Pairwise holder;
    int timeToLiveSec;

    public Issuer(Context context, Pairwise holder, int timeToLiveSec) {
        this.holder = holder;
        this.context = context;
        this.timeToLiveSec = timeToLiveSec;
    }

    public static class IssueParams {
        JSONObject values = null;
        Schema schema = null;
        CredentialDefinition credDef = null;
        String comment = null;
        String locale = BaseIssueCredentialMessage.DEF_LOCALE;
        List<ProposedAttrib> preview = new ArrayList<ProposedAttrib>();
        List<AttribTranslation> translation = new ArrayList<AttribTranslation>();
        String credId = null;

        public IssueParams() {}

        public IssueParams setValues(JSONObject values) {
            this.values = values;
            return this;
        }

        public IssueParams setSchema(Schema schema) {
            this.schema = schema;
            return this;
        }

        public IssueParams setCredDef(CredentialDefinition credDef) {
            this.credDef = credDef;
            return this;
        }

        public IssueParams setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public IssueParams setLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public IssueParams setPreview(List<ProposedAttrib> preview) {
            this.preview = preview;
            return this;
        }

        public IssueParams setTranslation(List<AttribTranslation> translation) {
            this.translation = translation;
            return this;
        }

        public IssueParams setCredId(String credId) {
            this.credId = credId;
            return this;
        }
    }

    public Boolean issue(IssueParams params) {
        if (params.values == null || params.schema == null || params.credDef == null || params.credId ==null)
            throw new RuntimeException("Bad params");

        try (AbstractP2PCoProtocol coprotocol = new CoProtocolP2P(context, holder, protocols(), timeToLiveSec)) {
            try {
                // Step-1: Send offer to holder
                Date expiresTime = new Date(System.currentTimeMillis() + this.timeToLiveSec * 1000L);
                JSONObject offer = context.getAnonCreds().issuerCreateCredentialOffer(params.credDef.getId());
                OfferCredentialMessage offerMsg = OfferCredentialMessage.builder().
                        setComment(params.comment).
                        setLocale(params.locale).
                        setOffer(offer).
                        setCredDef(new JSONObject(params.credDef.getBody().toString())).
                        setPreview(params.preview).
                        setIssuerSchema(params.schema.getBody()).
                        setTranslation(params.translation).
                        //setExpiresTime(expiresTime).
                                build();

                log.log(Level.INFO, "20% - Send offer");
                // Switch to await participant action

                Pair<Boolean, Message> okResp = coprotocol.sendAndWait(offerMsg);

                if (!(okResp.second instanceof RequestCredentialMessage)) {
                    throw new StateMachineTerminatedWithError(OFFER_PROCESSING_ERROR, "Unexpected @type: " + okResp.second.getType());
                }

                // Step-2: Create credential
                RequestCredentialMessage requestMsg = (RequestCredentialMessage) okResp.second;
                log.log(Level.INFO, "40% - Received credential request");

                JSONObject encodedCredValues = new JSONObject();
                for (String key : params.values.keySet()) {
                    JSONObject encCredVal = new JSONObject();
                    encCredVal.put("raw", params.values.get(key).toString());
                    encCredVal.put("encoded", Codec.encode(params.values.get(key)));
                    encodedCredValues.put(key, encCredVal);
                }

                log.log(Level.INFO, "70% - Build credential with values");
                Triple<JSONObject, String, JSONObject> createCredRes = context.
                        getAnonCreds().issuerCreateCredential(
                        offer, requestMsg.credRequest(), encodedCredValues);

                JSONObject cred = createCredRes.first;

                // Step-3: Issue and wait Ack
                IssueCredentialMessage issueMsg = IssueCredentialMessage.builder().
                        setComment(params.comment).
                        setLocale(params.locale).
                        setCred(cred).
                        setCredId(params.credId).
                        build();

                log.log(Level.INFO, "90% - Send Issue message");
                Pair<Boolean, Message> okAck = coprotocol.sendAndWait(issueMsg);

                if (!(okAck.second instanceof Ack)) {
                    throw new StateMachineTerminatedWithError(ISSUE_PROCESSING_ERROR, "Unexpected @type: " + okAck.second.getType());
                }

                log.log(Level.INFO, "100% - Issuing was terminated successfully");
                return true;
            } catch (StateMachineTerminatedWithError ex) {
                problemReport = IssueProblemReport.builder().
                        setProblemCode(ex.getProblemCode()).
                        setExplain(ex.getExplain()).
                        build();
                log.info("100% - Terminated with error. " + ex.getProblemCode() + " " + ex.getExplain());
                if (ex.isNotify())
                    coprotocol.send(problemReport);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
