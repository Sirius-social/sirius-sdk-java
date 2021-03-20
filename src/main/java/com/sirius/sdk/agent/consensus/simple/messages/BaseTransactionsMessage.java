package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class BaseTransactionsMessage extends SimpleConsensusMessage {

    static {
        Message.registerMessageClass(BaseTransactionsMessage.class, SimpleConsensusMessage.PROTOCOL, "stage");
    }

    public BaseTransactionsMessage(String msg) {
        super(msg);
    }

    public static abstract class Builder<B extends BaseInitLedgerMessage.Builder<B>> extends SimpleConsensusMessage.Builder<B> {
        List<Transaction> transactions = null;
        MicroLedgerState state = null;

        public B setTransactions(List<Transaction> transactions) {
            this.transactions = transactions;
            return self();
        }

        public B setState(MicroLedgerState state) {
            this.state = state;
            return self();
        }

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (this.transactions != null) {
                JSONArray trArr = new JSONArray();
                for (Transaction tr : transactions) {
                    trArr.put(tr.getJSONObject());
                }
                jsonObject.put("transactions", trArr);
            }

            if (this.state != null) {
                jsonObject.put("state", state.getJSONObject());
                jsonObject.put("hash", state.getHash());
            }

            return jsonObject;
        }
    }
}
