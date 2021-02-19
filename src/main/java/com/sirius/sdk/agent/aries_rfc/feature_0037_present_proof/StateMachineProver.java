package com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof;

import com.sirius.sdk.agent.AbstractStateMachine;
import com.sirius.sdk.agent.Ledger;
import com.sirius.sdk.agent.TransportLayer;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Holder;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import org.json.JSONObject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StateMachineProver extends BaseVerifyStateMachine {
    Pairwise verifier = null;
    Logger log = Logger.getLogger(Holder.class.getName());

    public StateMachineProver(Pairwise verifier, Ledger ledger, int timeToLive) {
        this.verifier = verifier;
    }

    boolean prove(RequestPresentationMessage request, String masterSecretId) {
        try {
            createCoprotocol(this.verifier);
            log.log(Level.INFO, "10% - Received proof request");
            request.validate();
            request.proofRequest();

        } finally {
            releaseCoprotocol();
        }
        return false;
    }

    class ExtractCredentialsInfoResult {
        List<JSONObject> cred_infos;
        List<JSONObject> schemas;
        List<JSONObject> credentialDefs;
        List<JSONObject> revStates;
    }

    private ExtractCredentialsInfoResult extractCredentialsInfo(JSONObject proofRequest, String poolName) {
        JSONObject proofResponse = context.agent.getWallet().getAnoncreds().proverSearchCredentialsForProofReq(proofRequest, 1);

        //proofResponse.get("requested_attributes")
        return null;
    }

}
