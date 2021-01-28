package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;


public class Issuer {

    Pairwise holder;
    Context context;
    int timeToLiveSec;

    public Issuer(Context context, Pairwise holder, int timeToLiveSec) {
        this.holder = holder;
        this.context = context;
        this.timeToLiveSec = timeToLiveSec;
    }

    public Future<Boolean> issue(Map<String, String> values, Schema schema, CredentialDefinition credDef,
                          String comment, String locate, List<ProposedAttrib> preview,
                          List<AttribTranslation> translation, String credId) {

        Agent a = context.agent;
        Date expiresTime = new Date(System.currentTimeMillis() + this.timeToLiveSec * 1000L);
        String offer = a.getWallet().getAnoncreds().issuerCreateCredentialOffer(credDef.getId());
        OfferCredentialMessage offerMsg = OfferCredentialMessage.create(comment, locate, offer, credDef.getBody(), preview,
                schema.getBody(), translation, expiresTime);



        return null;
    }
}
