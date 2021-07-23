package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines;

import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.errors.StateMachineTerminatedWithError;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.AttribTranslation;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.BasePresentProofMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.PresentProofProblemReport;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.PresentationMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.RequestPresentationMessage;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.CacheOptions;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2P;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of Verifier role for present-proof protocol
 *
 * See details: https://github.com/hyperledger/aries-rfcs/tree/master/features/0037-present-proof
 */
public class Verifier extends BaseVerifyStateMachine {
    Pairwise prover = null;
    Logger log = Logger.getLogger(Verifier.class.getName());
    String poolname;
    JSONObject requestedProof;

    public Verifier(Context context, Pairwise prover, Ledger ledger, int timeToLive) {
        this.context = context;
        this.prover = prover;
        this.poolname = ledger.getName();
        this.timeToLiveSec = timeToLive;
    }

    public Verifier(Context context, Pairwise prover, Ledger ledger) {
        this.context = context;
        this.prover = prover;
        this.poolname = ledger.getName();
    }

    public static class VerifyParams {
        /**
         * proof_request: Hyperledger Indy compatible proof-request
         */
        JSONObject proofRequest = null;

        /**
         * human readable attributes translations
         */
        List<AttribTranslation> translation = null;

        /**
         * human readable comment from Verifier to Prover
         */
        String comment = null;

        /**
         * locale, for example "en" or "ru"
         */
        String locale = BasePresentProofMessage.DEF_LOCALE;

        /**
         * 0037 protocol version, for example 1.0 or 1.1
         */
        String protocolVersion = null;

        public VerifyParams setProofRequest(JSONObject proofRequest) {
            this.proofRequest = proofRequest;
            return this;
        }

        public VerifyParams setTranslation(List<AttribTranslation> translation) {
            this.translation = translation;
            return this;
        }

        public VerifyParams setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public VerifyParams setLocale(String locale) {
            this.locale = locale;
            return this;
        }

        public VerifyParams setProtocolVersion(String protoVersion) {
            this.protocolVersion = protoVersion;
            return this;
        }
    }

    public boolean verify(VerifyParams params) {
        try (AbstractP2PCoProtocol coprotocol = new CoProtocolP2P(context, prover, protocols(), timeToLiveSec)) {
            try {
                // Step-1: Send proof request
                Date expiresTime = new Date(System.currentTimeMillis() + this.timeToLiveSec * 1000L);
                RequestPresentationMessage requestPresentationMessage = RequestPresentationMessage.builder().
                        setProofRequest(params.proofRequest).
                        setTranslation(params.translation).
                        setComment(params.comment).
                        setLocale(params.locale).
                        //setExpiresTime(expiresTime).
                                setVersion(params.protocolVersion).
                                build();
                requestPresentationMessage.setPleaseAck(true);

                log.log(Level.INFO, "30% - Send request");

                Pair<Boolean, Message> okMsg = coprotocol.sendAndWait(requestPresentationMessage);
                if (!(okMsg.second instanceof PresentationMessage)) {
                    throw new StateMachineTerminatedWithError(RESPONSE_NOT_ACCEPTED, "Unexpected @type: " + okMsg.second.getType());
                }

                log.log(Level.INFO, "60% - Presentation received");
                // Step-2 Verify
                PresentationMessage presentationMessage = (PresentationMessage) okMsg.second;
                JSONArray identifiers = presentationMessage.proof().optJSONArray("identifiers");
                if (identifiers == null)
                    identifiers = new JSONArray();

                JSONObject schemas = new JSONObject();
                JSONObject credentialDefs = new JSONObject();
                JSONObject revRegDefs = new JSONObject();
                JSONObject revRegs = new JSONObject();

                CacheOptions opts = new CacheOptions();

                for (Object o : identifiers) {
                    JSONObject identifier = (JSONObject) o;
                    String schemaId = identifier.optString("schema_id", "");
                    String credDefId = identifier.optString("cred_def_id", "");
                    String revRegId = identifier.optString("rev_reg_id", "");

                    if (!schemaId.isEmpty() && !schemas.has(schemaId)) {
                        schemas.put(schemaId, new JSONObject(
                                context.getCache().getSchema(poolname, prover.getMe().getDid(), schemaId, opts)));
                    }

                    if (!credDefId.isEmpty() && !credentialDefs.has(credDefId)) {
                        credentialDefs.put(credDefId, new JSONObject(
                                context.getCache().getCredDef(poolname, prover.getMe().getDid(), credDefId, opts)));
                    }
                }

                boolean success = context.getAnonCreds().verifierVerifyProof(
                        params.proofRequest, presentationMessage.proof(), schemas, credentialDefs, revRegDefs, revRegs);

                if (success) {
                    this.requestedProof = presentationMessage.proof().getJSONObject("requested_proof");
                    Ack ack = Ack.builder().setStatus(Ack.Status.OK).build();
                    ack.setThreadId(presentationMessage.hasPleaseAck() ? presentationMessage.getAckMessageId() : presentationMessage.getId());

                    log.log(Level.INFO, "100% - Verifying terminated successfully");
                    coprotocol.send(ack);
                    return true;
                } else {
                    throw new StateMachineTerminatedWithError(VERIFY_ERROR, "Verifying return false");
                }
            } catch (StateMachineTerminatedWithError ex) {
                problemReport = PresentProofProblemReport.builder().
                        setProblemCode(ex.getProblemCode()).
                        setExplain(ex.getExplain()).
                        build();
                log.info("100% - Terminated with error. " + ex.getProblemCode() + " " + ex.getExplain());
                if (ex.isNotify())
                    coprotocol.send(problemReport);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public JSONObject getRequestedProof() {
        return requestedProof;
    }
}
