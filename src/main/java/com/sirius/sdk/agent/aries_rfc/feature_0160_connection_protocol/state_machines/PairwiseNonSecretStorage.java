package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines;

import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;

class PairwiseNonSecretStorage {

    private static final String NON_SECRET_PERSISTENT_0160_PW = "NON_SECRET_PERSISTENT_0160_PW";

    public static Optional<JSONObject> optValueByConnectionKey(Context context, String connectionKeyBase58) {
        JSONObject query = new JSONObject();
        query.put("connectionKey", connectionKeyBase58);
        RetrieveRecordOptions opts = new RetrieveRecordOptions(false, true, false);
        Pair<List<String>, Integer> recordsTotal = context.getNonSecrets().walletSearch(NON_SECRET_PERSISTENT_0160_PW, query.toString(), opts, 1);
        if (recordsTotal.second != 1)
            return Optional.empty();
        return Optional.of(new JSONObject(new JSONObject(recordsTotal.first.get(0)).optString("value")));
    }

    public static Optional<String> optConnectionKeyByTheirVerkey(Context context, String theirVerkey) {
        JSONObject query = new JSONObject();
        query.put("theirVk", theirVerkey);
        RetrieveRecordOptions opts = new RetrieveRecordOptions(false, false, true);
        Pair<List<String>, Integer> recordsTotal = context.getNonSecrets().walletSearch(NON_SECRET_PERSISTENT_0160_PW, query.toString(), opts, 1);
        if (recordsTotal.second == 0)
            return Optional.empty();
        return Optional.of(new JSONObject(new JSONObject(recordsTotal.first.get(0)).optString("tags")).optString("connectionKey"));
    }

    public static void write(Context context, String connectionKeyBase58, JSONObject pairwise) {
        String theirVk = "";
        if (pairwise.has("their")) {
            theirVk = pairwise.optJSONObject("their").optString("verkey");
        }
        JSONObject tags = new JSONObject().
                put("connectionKey", connectionKeyBase58).
                put("theirVk", theirVk);

        if (optValueByConnectionKey(context, connectionKeyBase58).isPresent()) {
            context.getNonSecrets().updateWalletRecordValue(NON_SECRET_PERSISTENT_0160_PW, connectionKeyBase58, pairwise.toString());
            context.getNonSecrets().updateWalletRecordTags(NON_SECRET_PERSISTENT_0160_PW, connectionKeyBase58, tags.toString());
        } else {
            context.getNonSecrets().addWalletRecord(NON_SECRET_PERSISTENT_0160_PW, connectionKeyBase58, pairwise.toString(), tags.toString());
        }
    }

    public static void remove(Context context, String connectionKeyBase58) {
        if (optValueByConnectionKey(context, connectionKeyBase58).isPresent()) {
            context.getNonSecrets().deleteWalletRecord(NON_SECRET_PERSISTENT_0160_PW, connectionKeyBase58);
        }
    }
}
