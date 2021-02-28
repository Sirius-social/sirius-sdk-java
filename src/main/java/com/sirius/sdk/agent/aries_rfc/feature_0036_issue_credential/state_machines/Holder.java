package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines;

import com.sirius.sdk.agent.StateMachineTerminatedWithError;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.IssueCredentialMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.OfferCredentialMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.RequestCredentialMessage;
import com.sirius.sdk.agent.model.pairwise.Pairwise;

import java.util.logging.Logger;

import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.messaging.Type;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

public class Holder extends BaseIssuingStateMachine {
    Logger log = Logger.getLogger(Holder.class.getName());
    Pairwise issuer;

    public Holder(Context context, Pairwise issuer) {
        this.context = context;
        this.issuer = issuer;
    }

    public Pair<Boolean, String> accept(OfferCredentialMessage offer, String masterSecretId, String comment, String locale) {
        try {
            String docUri = Type.fromStr(offer.getType()).getDocUri();
            createCoprotocol(issuer);
            OfferCredentialMessage offerMsg = offer;

            // Step-1: Process Issuer Offer
            Pair<JSONObject, JSONObject> createCredReqRes = context.agent.getWallet().getAnoncreds().proverCreateCredentialReq(
                    issuer.getMe().getDid(), offerMsg.offer(), offer.credDef(), masterSecretId);

            JSONObject credRequest = createCredReqRes.first;
            JSONObject credMetadata = createCredReqRes.second;

            // Step-2: Send request to Issuer
            RequestCredentialMessage requestMsg = RequestCredentialMessage.builder().
                    setComment(comment).
                    setLocale(locale).
                    setCredRequest(credRequest).
                    build();

            Pair<Boolean, Message> okResp = coprotocol.wait(requestMsg);
            if (!(okResp.second instanceof IssueCredentialMessage)) {
                throw new StateMachineTerminatedWithError("request_not_accepted", "Unexpected @type:" + okResp.second.getType());
            }

            IssueCredentialMessage issueMsg = (IssueCredentialMessage) okResp.second;

            // Step-3: Store credential
            String credId = storeCredential(credMetadata, issueMsg.cred(), offer.credDef(), null, issueMsg.credId());

            Ack ack = Ack.builder().setStatus(Ack.Status.OK).setDocUri(docUri).build();
            ack.setThreadId(issueMsg.getAckMessageId());
            coprotocol.send(ack);
            return new Pair<Boolean, String>(true, credId);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            releaseCoprotocol();
        }

        return new Pair<Boolean, String>(false, "");
    }

    private String storeCredential(JSONObject credMetadata, JSONObject cred, JSONObject credDef, String revRegDef, String credId) {
        String credOrder = null;
        try {
            credOrder = context.agent.getWallet().getAnoncreds().proverGetCredential(credId);
        } catch (Exception ex) {}
        if (credOrder != null) {
            context.agent.getWallet().getAnoncreds().proverDeleteCredential(credId);
        }
        credId = context.agent.getWallet().getAnoncreds().proverStoreCredential(credId, credMetadata, cred, credDef, revRegDef);
        return credId;
    }
}
