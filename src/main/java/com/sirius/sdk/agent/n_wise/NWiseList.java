package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import org.apache.commons.lang.NotImplementedException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class NWiseList {
    AbstractNonSecrets nonSecrets;
    private static final String type = "NWiseList";

    class NWiseInfo {
        public String internalId;
        public String ledgerType;
        public JSONObject attach;
    }

    public NWiseList(AbstractNonSecrets nonSecrets) {
        this.nonSecrets = nonSecrets;
    }

    public String add(NWise nWise) {
        String internalId = UUID.randomUUID().toString();
        nonSecrets.addWalletRecord(type, internalId,
                new JSONObject().
                        put("ledgerType", nWise.getLedgerType()).
                        put("restoreAttach", nWise.getRestoreAttach()).toString()
                );
        return internalId;
    }

    public boolean remove(int i) {
        throw new NotImplementedException();
    }

    public List<NWiseInfo> getNWiseInfoList() {
        throw new NotImplementedException();
    }
}
