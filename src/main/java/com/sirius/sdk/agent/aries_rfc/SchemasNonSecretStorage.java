package com.sirius.sdk.agent.aries_rfc;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import org.json.JSONObject;

public class SchemasNonSecretStorage {

    public static void storeCredSchemaNonSecret(AbstractNonSecrets ns, JSONObject schema) {
        ns.addWalletRecord("schemas", schema.optString("id"), schema.toString());
    }

    public static void storeCredDefNonSecret(AbstractNonSecrets ns, JSONObject credDef) {
        ns.addWalletRecord("credDefs", credDef.getString("id"), credDef.toString());
    }

    public static JSONObject getCredSchemaNonSecret(AbstractNonSecrets ns, String id) {
        String record = ns.getWalletRecord("schemas", id, new RetrieveRecordOptions(true, true, false));
        if (record != null) {
            return new JSONObject((new JSONObject(record)).optString("value"));
        }
        return new JSONObject();
    }

    public static JSONObject getCredDefNonSecret(AbstractNonSecrets ns, String id) {
        String record = ns.getWalletRecord("credDefs", id, new RetrieveRecordOptions(true, true, false));
        if (record != null) {
            return new JSONObject((new JSONObject(record)).optString("value"));
        }
        return new JSONObject();
    }
}
