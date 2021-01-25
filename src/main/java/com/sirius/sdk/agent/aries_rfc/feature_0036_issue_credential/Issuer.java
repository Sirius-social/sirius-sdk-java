package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.impl.AnonCredsProxy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class Issuer {

    Pairwise holder;

    public Issuer(Pairwise holder, int timeToLiveSec) {
        this.holder = holder;
    }

    Future<Boolean> issue(Map<String, String> dict, Schema schema, CredentialDefinition credDef,
                          String comment, String locate, List<ProposedAttrib> preview,
                          List<AttribTranslation> translation, String credId) {
        AnonCredsProxy anonCreds = new AnonCredsProxy();

        return null;
    }
}
