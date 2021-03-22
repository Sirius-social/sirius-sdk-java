package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

import java.util.List;

/**
 * Message to process transactions propose by Actor
 */
public class ProposeTransactionsMessage extends BaseTransactionsMessage {

    static {
        Message.registerMessageClass(ProposeTransactionsMessage.class, SimpleConsensusMessage.PROTOCOL, "stage-propose");
    }

    public ProposeTransactionsMessage(String msg) {
        super(msg);
    }

    public int getTimeoutSec() {
        return getMessageObj().optInt("timeout_sec");
    }

    @Override
    public void validate() throws SiriusValidationError {
        super.validate();
        List<Transaction> txns = this.transactions();
        if (txns == null || txns.isEmpty()) {
            throw new SiriusValidationError("Empty transactions list");
        }
        for (Transaction txn : txns) {
            if (!txn.hasMetadata())
                throw new SiriusValidationError("Transaction has no metadata");
        }
        if (this.getState() == null) {
            throw new SiriusValidationError("Empty state");
        }
        if (this.getHash() == null || this.getHash().isEmpty()) {
            throw new SiriusValidationError("Empty hash");
        }
    }

    public static Builder<?> builder() {
        return new ProposeTransactionsMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends BaseTransactionsMessage.Builder<B> {
        Integer timeoutSec = null;

        public B setTimeoutSec(int timeoutSec) {
            this.timeoutSec = timeoutSec;
            return self();
        }

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (timeoutSec != null) {
                jsonObject.put("timeout_sec", timeoutSec);
            }

            return jsonObject;
        }

        public InitResponseLedgerMessage build() {
            return new InitResponseLedgerMessage(generateJSON().toString());
        }
    }

    private static class ProposeTransactionsMessageBuilder extends Builder<ProposeTransactionsMessageBuilder> {
        @Override
        protected ProposeTransactionsMessageBuilder self() {
            return this;
        }
    }
}
