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

    public Boolean issue(JSONObject values, Schema schema, CredentialDefinition credDef,
                         String comment, String locale, List<ProposedAttrib> preview,
                         List<AttribTranslation> translation, String credId) {
        try (AbstractP2PCoProtocol coprotocol = new CoProtocolP2P(context, holder, protocols(), timeToLiveSec)) {
            // Step-1: Send offer to holder
            Date expiresTime = new Date(System.currentTimeMillis() + this.timeToLiveSec * 1000L);
            JSONObject offer = context.getAnonCreds().issuerCreateCredentialOffer(credDef.getId());
            OfferCredentialMessage offerMsg = OfferCredentialMessage.builder().
                    setComment(comment).
                    setLocale(locale).
                    setOffer(offer).
                    setCredDef(new JSONObject(credDef.getBody().toString())).
                    setPreview(preview).
                    setIssuerSchema(schema.getBody()).
                    setTranslation(translation).
                    //setExpiresTime(expiresTime).
                    build();

            log.log(Level.INFO, "20% - Send offer");
            // Switch to await participant action

            Pair<Boolean, Message> okResp = coprotocol.sendAndWait(offerMsg);

            if (!(okResp.second instanceof RequestCredentialMessage)) {
                throw new StateMachineTerminatedWithError("offer_processing_error", "Unexpected @type: " + okResp.second.getType());
            }

            // Step-2: Create credential
            RequestCredentialMessage requestMsg = (RequestCredentialMessage) okResp.second;
            log.log(Level.INFO, "40% - Received credential request");

            JSONObject encodedCredValues = new JSONObject();
            for (String key : values.keySet()) {
                JSONObject encCredVal = new JSONObject();
                encCredVal.put("raw", values.get(key).toString());
                encCredVal.put("encoded", Codec.encode(values.get(key)));
                encodedCredValues.put(key, encCredVal);
            }

            log.log(Level.INFO, "70% - Build credential with values");
            Triple<JSONObject, String, JSONObject> createCredRes = context.
                    getAnonCreds().issuerCreateCredential(
                            offer, requestMsg.credRequest(), encodedCredValues);

            JSONObject cred = createCredRes.first;

            // Step-3: Issue and wait Ack
            IssueCredentialMessage issueMsg = IssueCredentialMessage.builder().
                    setComment(comment).
                    setLocale(locale).
                    setCred(cred).
                    setCredId(credId).
                    build();

            log.log(Level.INFO, "90% - Send Issue message");
            Pair<Boolean, Message> okAck = coprotocol.sendAndWait(issueMsg);

            if (!(okAck.second instanceof Ack)) {
                throw new StateMachineTerminatedWithError("issue_processing_error", "Unexpected @type: " + okAck.second.getType());
            }

            log.log(Level.INFO, "100% - Issuing was terminated successfully");
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
