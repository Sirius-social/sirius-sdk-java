package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines;

import com.sirius.sdk.agent.aries_rfc.SchemasNonSecretStorage;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.*;
import com.sirius.sdk.agent.ledger.CredentialDefinition;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.errors.StateMachineTerminatedWithError;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.pairwise.Pairwise;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import com.sirius.sdk.errors.indy_exceptions.WalletItemNotFoundException;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2P;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.messaging.Type;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

public class Holder extends BaseIssuingStateMachine {
    Logger log = Logger.getLogger(Holder.class.getName());
    Pairwise issuer;
    String masterSecretId;
    String locale;

    public Holder(Context context, Pairwise issuer, String masterSecretId, String locale) {
        this.context = context;
        this.issuer = issuer;
        this.masterSecretId = masterSecretId;
        this.locale = locale;
    }

    public Holder(Context context, Pairwise issuer, String masterSecretId) {
        this(context, issuer, masterSecretId, BaseIssueCredentialMessage.DEF_LOCALE);
    }

    public Pair<Boolean, String> accept(OfferCredentialMessage offer, String comment, Ledger ledger) {
        try (AbstractP2PCoProtocol coprotocol = new CoProtocolP2P(context, issuer, protocols(), timeToLiveSec)) {
            String docUri = Type.fromStr(offer.getType()).getDocUri();
            try {
                try {
                    offer.validate();
                } catch (SiriusValidationError e) {
                    throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, e.getMessage());
                }
                // Step-1: Process Issuer Offer
                OfferCredentialMessage.ParseResult parseResult = offer.parse(true);
                if (parseResult.offerBody == null) {
                    throw new StateMachineTerminatedWithError(OFFER_PROCESSING_ERROR, "Error while parsing cred_offer", true);
                }

                if (parseResult.credDefBody == null) {
                    if (ledger != null) {
                        CredentialDefinition credDef = ledger.loadCredDef(parseResult.offerBody.optString("cred_def_id"), this.issuer.getMe().getDid());
                        if (credDef.getBody() != null)
                            parseResult.credDefBody = new JSONObject(credDef.getBody().toString());
                    }
                }

                if (parseResult.credDefBody == null) {
                    throw new StateMachineTerminatedWithError(OFFER_PROCESSING_ERROR, "Error while parsing cred_def", true);
                }


                Pair<JSONObject, JSONObject> createCredReqRes = context.getAnonCreds().proverCreateCredentialReq(
                        issuer.getMe().getDid(), parseResult.offerBody, parseResult.credDefBody, masterSecretId);

                JSONObject credRequest = createCredReqRes.first;
                JSONObject credMetadata = createCredReqRes.second;

                // Step-2: Send request to Issuer
                RequestCredentialMessage requestMsg = RequestCredentialMessage.builder().
                        setComment(comment).
                        setLocale(locale).
                        setCredRequest(credRequest).
                        setVersion(offer.getVersion()).
                        build();

                if (offer.hasPleaseAck()) {
                    requestMsg.setThreadId(offer.getAckMessageId());
                } else {
                    requestMsg.setThreadId(offer.getId());
                }

                Pair<Boolean, Message> okResp = coprotocol.sendAndWait(requestMsg);
                if (!(okResp.second instanceof IssueCredentialMessage)) {
                    throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, "Unexpected @type:" + okResp.second.getType());
                }

                IssueCredentialMessage issueMsg = (IssueCredentialMessage) okResp.second;
                try {
                    issueMsg.validate();
                } catch (SiriusValidationError e) {
                    throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, e.getMessage());
                }

                // Step-3: Store credential
                String credId = storeCredential(credMetadata, issueMsg.cred(), parseResult.credDefBody, null, issueMsg.credId());
                storeMimeTypes(credId, offer.getCredentialPreview());
                SchemasNonSecretStorage.storeCredSchemaNonSecret(context.getNonSecrets(), offer.schema());
                SchemasNonSecretStorage.storeCredDefNonSecret(context.getNonSecrets(), offer.credDef());

                CredentialAck ack = CredentialAck.builder().
                        setStatus(Ack.Status.OK).
                        setDocUri(docUri).
                        setVersion(offer.getVersion()).
                        build();
                ack.setThreadId(issueMsg.getAckMessageId());
                coprotocol.send(ack);
                log.info("100% - Credential stored successfully");
                return new Pair<>(true, credId);
            } catch (StateMachineTerminatedWithError ex) {
                problemReport = IssueProblemReport.builder().
                        setProblemCode(ex.getProblemCode()).
                        setExplain(ex.getExplain()).
                        setDocUri(docUri).
                        build();
                log.info("100% - Terminated with error. " + ex.getProblemCode() + " " + ex.getExplain());
                if (ex.isNotify())
                    coprotocol.send(problemReport);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info("100% - Terminated with error");
        }

        return new Pair<>(false, "");
    }

    public Pair<Boolean, String> accept(OfferCredentialMessage offer) {
        return accept(offer, null, null);
    }

    public Pair<Boolean, String> accept(OfferCredentialMessage offer, Ledger ledger) {
        return accept(offer, null, ledger);
    }

    public Pair<Boolean, String> accept(OfferCredentialMessage offer, String comment) {
        return accept(offer, comment, null);
    }

    private String storeCredential(JSONObject credMetadata, JSONObject cred, JSONObject credDef, String revRegDef, String credId) {
        String credOrder;
        try {
            credOrder = context.getAnonCreds().proverGetCredential(credId);
        } catch (WalletItemNotFoundException ex) {
            credOrder = null;
        }
        if (credOrder != null) {
            context.getAnonCreds().proverDeleteCredential(credId);
        }
        credId = context.getAnonCreds().proverStoreCredential(credId, credMetadata, cred, credDef, revRegDef);
        return credId;
    }

    private void storeMimeTypes(String credId, List<ProposedAttrib> preview) {
        if (!preview.isEmpty()) {
            JSONObject mimeTypes = new JSONObject();
            for (ProposedAttrib attr : preview) {
                if (attr.has("mime-type")) {
                    mimeTypes.put(attr.optString("name"), attr.optString("mime-type"));
                }
            }
            if (!mimeTypes.isEmpty()) {
                JSONObject record = getMimeTypes(context, credId);
                if (!record.isEmpty()) {
                    context.getNonSecrets().deleteWalletRecord("mime-types", credId);
                }
                context.getNonSecrets().addWalletRecord("mime-types", credId,
                        new String(Base64.getEncoder().encode(mimeTypes.toString().getBytes(StandardCharsets.UTF_8))));
            }
        }
    }

    public static JSONObject getMimeTypes(Context c, String credId) {
        String record = c.getNonSecrets().getWalletRecord("mime-types", credId, new RetrieveRecordOptions(true, true, false));
        if (record != null) {
            JSONObject rec = new JSONObject(record);
            String b64 = rec.optString("value");
            String vals = new String(Base64.getDecoder().decode(b64.getBytes(StandardCharsets.UTF_8)));
            return new JSONObject(vals);
        }
        return new JSONObject();
    }
}
