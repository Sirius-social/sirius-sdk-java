package com.sirius.sdk.agent.diddoc;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.List;

public abstract class PublicDidDoc extends DidDoc {
    public static final String NON_SECRET_WALLET_NAME = "PublicDidDoc";

    public abstract boolean submitToLedger(Context context);

    public void saveToWallet(AbstractNonSecrets nonSecrets) {
        JSONObject tags = new JSONObject().
                put("tag1", NON_SECRET_WALLET_NAME).
                put("id", getDid());
        RetrieveRecordOptions opts = new RetrieveRecordOptions(false, false, false);
        if (nonSecrets.walletSearch(NON_SECRET_WALLET_NAME, tags.toString(), opts, 1).second == 0) {
            nonSecrets.addWalletRecord(NON_SECRET_WALLET_NAME, getDid(), payload.toString(), tags.toString());
        } else {
            nonSecrets.updateWalletRecordValue(NON_SECRET_WALLET_NAME, getDid(), payload.toString());
        }
    }
}
