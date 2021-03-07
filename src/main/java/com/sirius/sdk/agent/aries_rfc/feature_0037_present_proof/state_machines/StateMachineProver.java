package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines;

import com.sirius.sdk.agent.Ledger;
import com.sirius.sdk.agent.StateMachineTerminatedWithError;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.PresentProofProblemReport;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.PresentationMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.RequestPresentationMessage;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.CacheOptions;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StateMachineProver extends BaseVerifyStateMachine {
    Pairwise verifier = null;
    String poolName;
    Logger log = Logger.getLogger(StateMachineProver.class.getName());

    public StateMachineProver(Context context, Pairwise verifier, Ledger ledger, int timeToLiveSec) {
        this.context = context;
        this.verifier = verifier;
        this.poolName = ledger.getName();
        this.timeToLiveSec = timeToLiveSec;
    }

    public StateMachineProver(Context context, Pairwise verifier, Ledger ledger) {
        this.context = context;
        this.verifier = verifier;
        this.poolName = ledger.getName();
    }

    public boolean prove(RequestPresentationMessage request, String masterSecretId) {
        try {
            createCoprotocol(this.verifier);
            // Step-1: Process proof-request
            log.log(Level.INFO, "10% - Received proof request");
            request.validate();
            request.proofRequest();

            ExtractCredentialsInfoResult credInfoRes = extractCredentialsInfo(request.proofRequest(), poolName);

            // Step-2: Build proof
            JSONObject proof = context.getAnonCreds().proverCreateProof(
                    request.proofRequest(), credInfoRes.credInfos, masterSecretId, credInfoRes.schemas, credInfoRes.credentialDefs, credInfoRes.revStates);

            // Step-3: Send proof and wait Ack to check success from Verifier side
            PresentationMessage presentationMessage = PresentationMessage.builder()
                    .setProof(proof)
                    .setVersion(request.getVersion())
                    .build();
            presentationMessage.setPleaseAck(true);
            if (request.hasPleaseAck()) {
                presentationMessage.setThreadId(request.getAckMessageId());
            }

            // Step-3: Wait ACK
            log.log(Level.INFO, "50% - Send presentation");

            // Switch to await participant action
            Pair<Boolean, Message> okMsg = coprotocol.wait(presentationMessage);

            if (okMsg.second instanceof Ack) {
                log.log(Level.INFO, "100% - Verify OK!");
                return true;
            } else if (okMsg.second instanceof PresentProofProblemReport) {
                log.log(Level.INFO, "100% - Verify ERROR!");
                return false;
            } else {
                throw new StateMachineTerminatedWithError("response_for_unknown_request", "Unexpected response @type:" + okMsg.second.getType().toString());
            }
        } catch (SiriusPendingOperation | SiriusInvalidPayloadStructure | SiriusInvalidMessage siriusPendingOperation) {
            siriusPendingOperation.printStackTrace();
        } catch (StateMachineTerminatedWithError stateMachineTerminatedWithError) {
            stateMachineTerminatedWithError.printStackTrace();
        } finally {
            releaseCoprotocol();
        }
        return false;
    }

    static class ExtractCredentialsInfoResult {
        JSONObject credInfos = new JSONObject();
        JSONObject schemas = new JSONObject();
        JSONObject credentialDefs = new JSONObject();
        JSONObject revStates = new JSONObject();
    }

    private ExtractCredentialsInfoResult extractCredentialsInfo(JSONObject proofRequest, String poolName) {
        JSONObject proofResponse = context.getAnonCreds().proverSearchCredentialsForProofReq(proofRequest, 1);
        ExtractCredentialsInfoResult res = new ExtractCredentialsInfoResult();
        CacheOptions opts = new CacheOptions();
        res.credInfos.put("self_attested_attributes", new JSONObject());
        res.credInfos.put("requested_attributes", new JSONObject());
        res.credInfos.put("requested_predicates", new JSONObject());

        List<JSONObject> allInfos = new ArrayList<JSONObject>();
        JSONObject requestedAttributes = proofResponse.getJSONObject("requested_attributes");
        for (String referentId : requestedAttributes.keySet()) {
            JSONArray credInfos = requestedAttributes.getJSONArray(referentId);
            JSONObject credInfo = credInfos.getJSONObject(0).getJSONObject("cred_info");
            JSONObject info = new JSONObject();
            info.put("cred_id", credInfo.getString("referent"));
            info.put("revealed", true);
            res.credInfos.getJSONObject("requested_attributes").put(referentId, info);
            allInfos.add(credInfo);
        }

        JSONObject requestedPredicates = proofResponse.getJSONObject("requested_predicates");
        for (String referentId : requestedAttributes.keySet()) {
            JSONArray predicates = requestedAttributes.getJSONArray(referentId);
            JSONObject predInfo = predicates.getJSONObject(0).getJSONObject("cred_info");
            JSONObject info = new JSONObject();
            info.put("cred_id", predInfo.getString("referent"));
            res.credInfos.getJSONObject("requested_predicates").put(referentId, info);
            allInfos.add(predInfo);
        }

        for (JSONObject credInfo : allInfos) {
            String schemaId = credInfo.getString("schema_id");
            String credDefId = credInfo.getString("cred_def_id");
            JSONObject schema = new JSONObject(context.getCache().getSchema(poolName, this.verifier.getMe().getDid(), schemaId, opts));
            res.schemas.put(schemaId, schema);
            JSONObject credDef = new JSONObject(context.getCache().getCredDef(poolName, this.verifier.getMe().getDid(), credDefId, opts));
            res.credentialDefs.put(credDefId, credDef);
        }

        return res;
    }

}
