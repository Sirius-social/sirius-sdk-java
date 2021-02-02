package com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential;

import com.sirius.sdk.agent.model.pairwise.Pairwise;
import java.util.concurrent.Future;

import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Type;
import com.sirius.sdk.utils.Pair;

public class Holder extends BaseIssuingStateMachine {
    Pairwise issuer;

    public Holder(Context context, Pairwise issuer) {
        this.context = context;
        this.issuer = issuer;
    }

    Future<Pair<Boolean, String>> accept(OfferCredentialMessage offer, String masterSecretId, String comment, String locate) {
        try {
            String docUri = Type.fromStr(offer.getType()).getDocUri();
            createCoprotocol(issuer);
            OfferCredentialMessage offerMsg = offer;

            Pair<String, String> createCredReqRes = context.agent.getWallet().getAnoncreds().proverCreateCredentialReq(
                    issuer.getMe().getDid(), offerMsg.offer().toString(), offer.credDef().toString(), masterSecretId);

            String credRequest = createCredReqRes.first;
            RequestCredentialMessage requestMsg = RequestCredentialMessage.create(comment, locate, credRequest, docUri);

            coprotocol.wait(requestMsg);


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

        }

        return null;
    }
}
