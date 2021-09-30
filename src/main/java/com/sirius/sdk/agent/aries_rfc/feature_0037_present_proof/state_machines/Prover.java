package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines;

import com.sirius.sdk.agent.aries_rfc.SchemasNonSecretStorage;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.PresentationAck;
import com.sirius.sdk.errors.StateMachineTerminatedWithError;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.PresentProofProblemReport;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.PresentationMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.RequestPresentationMessage;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.CacheOptions;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2P;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Prover extends BaseVerifyStateMachine {
    Pairwise verifier = null;
    String poolName;
    String masterSecretId;
    Logger log = Logger.getLogger(Prover.class.getName());

    public Prover(Context context, Pairwise verifier, String masterSecretId, String poolName) {
        this.context = context;
        this.verifier = verifier;
        this.poolName = poolName;
        this.masterSecretId = masterSecretId;
    }

    public Prover(Context context, Pairwise verifier, String masterSecretId) {
        this(context, verifier, masterSecretId, null);
    }

    public boolean prove(RequestPresentationMessage request, JSONObject selfAttestedAttributes) {
        try (AbstractP2PCoProtocol coprotocol = new CoProtocolP2P(context, verifier, protocols(), timeToLiveSec)) {
            try {
                // Step-1: Process proof-request
                log.log(Level.INFO, "10% - Received proof request");
                try {
                    request.validate();
                } catch (SiriusValidationError e) {
                    throw new StateMachineTerminatedWithError(REQUEST_NOT_ACCEPTED, e.getMessage());
                }

                ExtractCredentialsInfoResult credInfoRes = extractCredentialsInfo(request.proofRequest(), poolName, selfAttestedAttributes);

                // Step-2: Build proof
                JSONObject proof = context.getAnonCreds().proverCreateProof(
                        request.proofRequest(), credInfoRes.credInfos, masterSecretId, credInfoRes.schemas,
                        credInfoRes.credentialDefs, credInfoRes.revStates);

                // Step-3: Send proof and wait Ack to check success from Verifier side
                PresentationMessage presentationMessage = PresentationMessage.builder()
                        .setProof(proof)
                        .setVersion(request.getVersion())
                        .build();
                presentationMessage.setPleaseAck(true);
                if (request.hasPleaseAck()) {
                    presentationMessage.setThreadId(request.getAckMessageId());
                } else {
                    presentationMessage.setThreadId(request.getId());
                }

                // Step-3: Wait ACK
                log.log(Level.INFO, "50% - Send presentation");

                // Switch to await participant action
                Pair<Boolean, Message> okMsg = coprotocol.sendAndWait(presentationMessage);

                if (okMsg.second instanceof Ack) {
                    log.log(Level.INFO, "100% - Verify OK!");
                    return true;
                } else if (okMsg.second instanceof PresentProofProblemReport) {
                    log.log(Level.INFO, "100% - Verify ERROR!");
                    return false;
                } else {
                    throw new StateMachineTerminatedWithError(RESPONSE_FOR_UNKNOWN_REQUEST, "Unexpected response @type:" + okMsg.second.getType().toString());
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

    public boolean prove(RequestPresentationMessage request) {
        return prove(request, new JSONObject());
    }

    static class ExtractCredentialsInfoResult {
        JSONObject credInfos = new JSONObject();
        JSONObject schemas = new JSONObject();
        JSONObject credentialDefs = new JSONObject();
        JSONObject revStates = new JSONObject();
    }

    private ExtractCredentialsInfoResult extractCredentialsInfo(JSONObject proofRequest, String poolName, JSONObject selfAttestedAttrs) {
        JSONObject proofResponse = context.getAnonCreds().proverSearchCredentialsForProofReq(proofRequest, 1);
        ExtractCredentialsInfoResult res = new ExtractCredentialsInfoResult();
        CacheOptions opts = new CacheOptions();
        res.credInfos.put("self_attested_attributes", new JSONObject());
        res.credInfos.put("requested_attributes", new JSONObject());
        res.credInfos.put("requested_predicates", new JSONObject());
        JSONObject requestedAttributesWithNoRestrictions = new JSONObject();

        if (proofResponse == null) {
            return res;
        }

        JSONObject requestedAttributes = proofRequest.getJSONObject("requested_attributes");
        for (String referentId : requestedAttributes.keySet()) {
            JSONObject data = requestedAttributes.getJSONObject(referentId);
            boolean hasRestrictions = data.has("restrictions");
            if (!hasRestrictions) {
                if (data.has("names")) {
                    requestedAttributesWithNoRestrictions.put(referentId, data.get("names"));
                }
                if (data.has("name")) {
                    requestedAttributesWithNoRestrictions.put(referentId, new JSONArray().put(data.get("name")));
                }
            }
        }

        requestedAttributes = proofResponse.getJSONObject("requested_attributes");
        List<JSONObject> allInfos = new ArrayList<JSONObject>();
        for (String referentId : requestedAttributes.keySet()) {
            if (requestedAttributesWithNoRestrictions.has(referentId)) {
                JSONArray attrNames = requestedAttributesWithNoRestrictions.getJSONArray(referentId);
                for (Object o : attrNames) {
                    String attrName = (String) o;
                    if (selfAttestedAttrs.has(attrName)) {
                        res.credInfos.getJSONObject("self_attested_attributes").put(referentId, selfAttestedAttrs.get(attrName));
                    } else {
                        res.credInfos.getJSONObject("self_attested_attributes").put(referentId, "");
                    }
                }
            }

            JSONArray credInfos = requestedAttributes.getJSONArray(referentId);
            JSONObject credInfo = credInfos.getJSONObject(0).getJSONObject("cred_info");
            JSONObject info = new JSONObject();
            info.put("cred_id", credInfo.getString("referent"));
            info.put("revealed", true);
            res.credInfos.getJSONObject("requested_attributes").put(referentId, info);
            allInfos.add(credInfo);
        }

        JSONObject requestedPredicates = proofResponse.getJSONObject("requested_predicates");
        for (String referentId : requestedPredicates.keySet()) {
            JSONArray predicates = requestedPredicates.getJSONArray(referentId);
            if (!predicates.isEmpty()) {
                JSONObject predInfo = predicates.getJSONObject(0).getJSONObject("cred_info");
                JSONObject info = new JSONObject();
                info.put("cred_id", predInfo.getString("referent"));
                res.credInfos.getJSONObject("requested_predicates").put(referentId, info);
                allInfos.add(predInfo);
            }
        }

        for (JSONObject credInfo : allInfos) {
            String schemaId = credInfo.getString("schema_id");
            String credDefId = credInfo.getString("cred_def_id");
            JSONObject schema = null;
            if (poolName != null) {
                schema = new JSONObject(context.getCache().getSchema(poolName, this.verifier.getMe().getDid(), schemaId, opts));
            } else {
                schema = SchemasNonSecretStorage.getCredSchemaNonSecret(context.getNonSecrets(), schemaId);
            }
            res.schemas.put(schemaId, schema);

            JSONObject credDef = null;
            if (poolName != null) {
                credDef = new JSONObject(context.getCache().getCredDef(poolName, this.verifier.getMe().getDid(), credDefId, opts));
            } else {
                credDef = SchemasNonSecretStorage.getCredDefNonSecret(context.getNonSecrets(), credDefId);
            }
            res.credentialDefs.put(credDefId, credDef);
        }

        return res;
    }
}
