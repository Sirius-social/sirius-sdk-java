package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.StateMachineTerminatedWithError;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.rpc.AddressedTunnel;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
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

    public Boolean issue(Map<String, String> values, Schema schema, CredentialDefinition credDef,
                          String comment, String locate, List<ProposedAttrib> preview,
                          List<AttribTranslation> translation, String credId) {
        try {
            createCoprotocol(holder);

            // Step-1: Send offer to holder
            Date expiresTime = new Date(System.currentTimeMillis() + this.timeToLiveSec * 1000L);
            String offer = context.agent.getWallet().getAnoncreds().issuerCreateCredentialOffer(credDef.getId());
            OfferCredentialMessage offerMsg = OfferCredentialMessage.create(comment, locate, offer, credDef.getBody(), preview,
                    schema.getBody(), translation, expiresTime);

            log.log(Level.INFO, "20% - Send offer");
            // Switch to await participant action
            Pair<Boolean, Message> okResp = coprotocol.wait(offerMsg);

            if (!(okResp.second instanceof RequestCredentialMessage)) {
                throw new StateMachineTerminatedWithError("offer_processing_error", "Unexpected @type: " + okResp.second.getType());
            }
            RequestCredentialMessage resp = (RequestCredentialMessage) okResp.second;

            // Step-2: Create credential
            RequestCredentialMessage requestMsg = resp;
            log.log(Level.INFO, "40% - Received credential request");

            JSONObject encodedCredValues = new JSONObject();
            for (Map.Entry<String, String> entry : values.entrySet()) {
                encodedCredValues.put(entry.getKey(), entry.getValue());
            }

            log.log(Level.INFO, "70% - Build credential with values");
            Triple<String, String, String> createCredRes = context.agent.getWallet().
                    getAnoncreds().issuerCreateCredential(
                            offer, requestMsg.credRequest().toString(), encodedCredValues.toString());

            String cred = createCredRes.first;

            // Step-3: Issue and wait Ack
            IssueCredentialMessage issueMsg = IssueCredentialMessage.create(comment, locate, cred, credId);

            log.log(Level.INFO, "90% - Send Issue message");
            Pair<Boolean, Message> okAck = coprotocol.wait(issueMsg);

            if (!(okAck.second instanceof Ack)) {
                throw new StateMachineTerminatedWithError("issue_processing_error", "Unexpected @type: " + okAck.second.getType());
            }

            log.log(Level.INFO, "100% - Issuing was terminated successfully");
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            releaseCoprotocol();
        }

        return false;
    }
}
