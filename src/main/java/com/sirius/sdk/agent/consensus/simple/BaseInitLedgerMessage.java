package com.sirius.sdk.agent.consensus.simple;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import com.sirius.sdk.agent.microledgers.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class BaseInitLedgerMessage extends SimpleConsensusMessage {
    public BaseInitLedgerMessage(String msg) {
        super(msg);
    }

    public static abstract class Builder<B extends Builder<B>> extends SimpleConsensusMessage.Builder<B> {
        String ledgerName = null;
        String rootHash = null;
        List<Transaction> genesis = null;

        public B setLedgerName(String ledgerName) {
            this.ledgerName = ledgerName;
            return self();
        }

        public B setRootHash(String rootHash) {
            this.rootHash = rootHash;
            return self();
        }

        public B setGenesis(List<Transaction> genesis) {
            this.genesis = genesis;
            return self();
        }

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            JSONObject ledger = new JSONObject();
            if (ledgerName != null) {
                ledger.put("name", ledgerName);
            }
            if (rootHash != null) {
                ledger.put("root_hash", rootHash);
            }

            if (genesis != null) {
                JSONArray gArr = new JSONArray();
                for (Transaction tr : genesis) {
                    gArr.put(tr.getJSONObject());
                }
                ledger.put("genesis", gArr);
            }

            if (!ledger.isEmpty()) {
                jsonObject.put("ledger", ledger);
            }

            return jsonObject;
        }
    }
}
