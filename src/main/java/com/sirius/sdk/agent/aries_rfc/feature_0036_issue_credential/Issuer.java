package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.StateMachineTerminatedWithError;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.model.coprotocols.PairwiseCoProtocolTransport;
import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;


public class Issuer {

    AbstractCoProtocolTransport coprotocol = null;

    Pairwise holder;
    Context context;
    int timeToLiveSec;

    public Issuer(Context context, Pairwise holder, int timeToLiveSec) {
        this.holder = holder;
        this.context = context;
        this.timeToLiveSec = timeToLiveSec;
    }

    private void createCoprotocol(Pairwise holder) {
        if (coprotocol == null) {
            coprotocol = context.agent.spawn(holder);
            coprotocol.start(Arrays.asList(BaseIssueCredentialMessage.PROTOCOL, Ack.PROTOCOL));
        }
    }

    private void releaseCoprotocol() {
        coprotocol.stop();
        coprotocol = null;
    }

    public Future<Boolean> issue(Map<String, String> values, Schema schema, CredentialDefinition credDef,
                          String comment, String locate, List<ProposedAttrib> preview,
                          List<AttribTranslation> translation, String credId) {
        try {
            createCoprotocol(holder);

            Date expiresTime = new Date(System.currentTimeMillis() + this.timeToLiveSec * 1000L);
            String offer = context.agent.getWallet().getAnoncreds().issuerCreateCredentialOffer(credDef.getId());
            OfferCredentialMessage offerMsg = OfferCredentialMessage.create(comment, locate, offer, credDef.getBody(), preview,
                    schema.getBody(), translation, expiresTime);
            Pair<Boolean, Message> okResp = coprotocol.wait(offerMsg);

            if (!(okResp.second instanceof RequestCredentialMessage)) {
                throw new StateMachineTerminatedWithError("offer_processing_error", "Unexpected @type: " + okResp.second.getType());
            }
            RequestCredentialMessage resp = (RequestCredentialMessage) okResp.second;
            RequestCredentialMessage requestMsg = resp;

            //context.agent.getWallet().getAnoncreds().issuerCreateCredential(offer, requestMsg.cre)


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            releaseCoprotocol();
        }

        return null;
    }
}
