package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class Issuer {

    Pairwise holder;
    Context context;

    public Issuer(Context context, Pairwise holder, int timeToLiveSec) {
        this.holder = holder;
        this.context = context;
    }

    public Future<Boolean> issue(Map<String, String> values, Schema schema, CredentialDefinition credDef,
                          String comment, String locate, List<ProposedAttrib> preview,
                          List<AttribTranslation> translation, String credId) {

        Agent a = context.agent;
        String offer = a.getWallet().getAnoncreds().issuerCreateCredentialOffer(credDef.getId());
        OfferCredentialMessage offer_msg = new OfferCredentialMessage();

        return null;
    }
}
