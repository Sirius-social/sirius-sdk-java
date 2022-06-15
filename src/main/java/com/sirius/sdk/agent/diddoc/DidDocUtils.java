package com.sirius.sdk.agent.diddoc;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.sirius.sdk.agent.diddoc.PublicDidDoc.NON_SECRET_WALLET_NAME;

public class DidDocUtils {

    public static PublicDidDoc resolve(String did) {
        if (did.startsWith("did:iota:")) {
            return IotaPublicDidDoc.load(did);
        }
        return null;
    }

    public static List<String> publicDidList(AbstractNonSecrets ns) {
        JSONObject query = new JSONObject();
        query.put("tag1", NON_SECRET_WALLET_NAME);
        RetrieveRecordOptions opts = new RetrieveRecordOptions(false, false, false);
        Pair<List<String>, Integer> recordsTotal = ns.walletSearch(NON_SECRET_WALLET_NAME, query.toString(), opts, 10000);
        List<String> res = new ArrayList<>();
        for (String s : recordsTotal.first) {
            res.add(new JSONObject(s).optString("id"));
        }
        return res;
    }

    public static DidDoc fetchFromWallet(String did, AbstractNonSecrets ns) {
        String record = ns.getWalletRecord(NON_SECRET_WALLET_NAME, did, new RetrieveRecordOptions(true, true, true));
        if (record != null) {
            return new DidDoc(new JSONObject((new JSONObject(record)).optString("value")));
        }
        return null;
    }
}
