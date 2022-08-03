package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.utils.Pair;
import org.apache.commons.lang.NotImplementedException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class NWiseList {
    AbstractNonSecrets nonSecrets;
    private static final String NON_SECRET_NWISE_LIST = "NWiseList";
    private static final String NON_SECRET_NWISE_LIST_INVITATION_KEYS = "NWiseListInvitationKeys";

    public static class NWiseInfo {
        public String internalId;
        public String ledgerType;
        public JSONObject attach;
    }

    public NWiseList(AbstractNonSecrets nonSecrets) {
        this.nonSecrets = nonSecrets;
    }

    public String add(NWise nWise) {
        String internalId = UUID.randomUUID().toString();
        JSONObject tags = new JSONObject().put("type", NON_SECRET_NWISE_LIST);
        nonSecrets.addWalletRecord(NON_SECRET_NWISE_LIST, internalId,
                new JSONObject().
                        put("ledgerType", nWise.getLedgerType()).
                        put("restoreAttach", nWise.getRestoreAttach()).toString(),
                tags.toString()
                );
        return internalId;
    }

    public void clearList() {
        List<NWiseInfo> list = getNWiseInfoList();
        for (NWiseInfo info : list) {
            nonSecrets.deleteWalletRecord(NON_SECRET_NWISE_LIST, info.internalId);
        }
    }

    public void remove(String internalId) {
        nonSecrets.deleteWalletRecord(NON_SECRET_NWISE_LIST, internalId);
    }

    public List<NWiseInfo> getNWiseInfoList() {
        JSONObject tags = new JSONObject().put("type", NON_SECRET_NWISE_LIST);
        Pair<List<String>,Integer> record = nonSecrets.walletSearch(NON_SECRET_NWISE_LIST, tags.toString(),
                new RetrieveRecordOptions(false, true, false), 100000);
        List<NWiseInfo> res = new ArrayList<>();
        if (record.second == 0)
            return res;
         for (String s : record.first) {
             JSONObject o = new JSONObject(s);
             NWiseInfo info = new NWiseInfo();
             info.internalId = o.optString("id");
             JSONObject value = new JSONObject(o.optString("value"));
             info.ledgerType = value.optString("ledgerType");
             info.attach = value.optJSONObject("restoreAttach");
             res.add(info);
         }
        return res;
    }

    public NWiseInfo getNWiseInfo(String internalId) {
        String res = nonSecrets.getWalletRecord(NON_SECRET_NWISE_LIST, internalId, new RetrieveRecordOptions(true, true, true));
        if (res == null || res.isEmpty())
            return null;
        JSONObject o = new JSONObject(res);
        NWiseInfo info = new NWiseInfo();
        info.internalId = internalId;
        info.ledgerType = new JSONObject(o.optString("value")).optString("ledgerType");
        info.attach = new JSONObject(o.optString("value")).optJSONObject("restoreAttach");
        return info;
    }

    public boolean addInvitationKey(String internalId, String keyBase58) {
        if (!hasInvitationKey(keyBase58)) {
            JSONObject tags = new JSONObject().put("invitationKeyBase58", keyBase58);
            nonSecrets.addWalletRecord(
                    NON_SECRET_NWISE_LIST_INVITATION_KEYS,
                    keyBase58,
                    new JSONObject().put("internalId", internalId).toString(),
                    tags.toString());
        }
        return true;
    }

    public boolean hasInvitationKey(String keyBase58) {
        JSONObject tags = new JSONObject().put("invitationKeyBase58", keyBase58);
        Pair<List<String>,Integer> record = nonSecrets.walletSearch(NON_SECRET_NWISE_LIST_INVITATION_KEYS, tags.toString(),
                new RetrieveRecordOptions(true, true, true), 1);
        return record.second != 0;
    }

    public NWiseInfo getNWiseInfoByInvitation(String keyBase58) {
        JSONObject tags = new JSONObject().put("invitationKeyBase58", keyBase58);
        Pair<List<String>,Integer> record = nonSecrets.walletSearch(NON_SECRET_NWISE_LIST_INVITATION_KEYS, tags.toString(),
                new RetrieveRecordOptions(true, true, true), 1);
        if (record.second > 0) {
            String id = new JSONObject(new JSONObject(record.first.get(0)).getString("value")).optString("internalId");
            return getNWiseInfo(id);
        }
        return null;
    }

    public boolean removeInvitationKey(String keyBase58) {
        if (!hasInvitationKey(keyBase58)) {
            return false;
        }

        nonSecrets.deleteWalletRecord(NON_SECRET_NWISE_LIST_INVITATION_KEYS, keyBase58);
        return true;
    }
}
