package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BaseTransactionsMessage extends SimpleConsensusMessage {

    static {
        Message.registerMessageClass(BaseTransactionsMessage.class, SimpleConsensusMessage.PROTOCOL, "stage");
    }

    public BaseTransactionsMessage(String msg) {
        super(msg);
    }

    public List<Transaction> transactions() {
        JSONArray trArr = getMessageObj().optJSONArray("transactions");
        if (trArr != null) {
            List<Transaction> res = new ArrayList<>();
            for (Object o : trArr) {
                res.add(new Transaction((JSONObject) o));
            }
            return res;
        }
        return null;
    }

    public MicroLedgerState getState() {
        JSONObject jsonState = getMessageObj().optJSONObject("state");
        if (jsonState != null) {
            MicroLedgerState state = new MicroLedgerState(jsonState);
            if (state.isFilled())
                return state;
        }
        return null;
    }

    public String getHash() {
        return getMessageObj().optString("hash", null);
    }

    public static abstract class Builder<B extends Builder<B>> extends SimpleConsensusMessage.Builder<B> {
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
                    trArr.put(tr);
                }
                jsonObject.put("transactions", trArr);
            }

            if (this.state != null) {
                jsonObject.put("state", state);
                jsonObject.put("hash", state.getHash());
            }

            return jsonObject;
        }
    }
}
