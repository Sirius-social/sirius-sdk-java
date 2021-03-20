package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.aries_rfc.Utils;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Arrays;


public class InitRequestLedgerMessage extends BaseInitLedgerMessage {

    static {
        Message.registerMessageClass(InitRequestLedgerMessage.class, SimpleConsensusMessage.PROTOCOL, "initialize-request");
    }

    public InitRequestLedgerMessage(String msg) {
        super(msg);
    }

    public void addSignature(AbstractCrypto api, Pairwise.Me me) throws SiriusContextError {
        if (!this.getParticipants().contains(me.getDid())) {
            throw new SiriusContextError("Signer must be a participant");
        }
        if (this.ledgerHash() != null) {
            JSONObject hashSignature = Utils.sign(api, this.ledgerHash(), me.getVerkey());
            JSONArray signatures = this.signatures();
            for (int i = signatures.length()-1; i >=0; i--) {
                if (signatures.getJSONObject(i).optString("participant").equals(me.getDid())) {
                    signatures.remove(i);
                }
            }
            signatures.put(new JSONObject().
                    put("participant", me.getDid()).
                    put("signature", hashSignature));
            getMessageObj().put("signatures", signatures);
        } else {
            throw new SiriusContextError("Ledger Hash description is empty");
        }
    }

    public void checkLedgerHash() throws SiriusContextError {
        if (this.ledgerHash() == null)
            throw new SiriusContextError("Ledger hash is empty");
        if (this.getLedger() != null)
            throw new SiriusContextError("Ledger body is empty");
    }

    @Override
    public void validate() throws SiriusValidationError {
        super.validate();
        if (this.getLedger() != null)
            throw new SiriusValidationError("Ledger body is empty");
        if (!this.getLedger().keySet().containsAll(Arrays.asList("root_hash", "name", "genesis")))
            throw new SiriusValidationError("Expected field does not exists in Ledger container");
        if (this.ledgerHash() == null)
            throw new SiriusValidationError("Ledger hash is empty");
        if (!this.ledgerHash().keySet().containsAll(Arrays.asList("func", "base58")))
            throw new SiriusValidationError("Expected field does not exists in Ledger Hash");
    }

    public static Builder<?> builder() {
        return new InitRequestLedgerMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends BaseInitLedgerMessage.Builder<B> {
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

        public InitRequestLedgerMessage build() {
            return new InitRequestLedgerMessage(generateJSON().toString());
        }
    }

    private static class InitRequestLedgerMessageBuilder extends Builder<InitRequestLedgerMessageBuilder> {
        @Override
        protected InitRequestLedgerMessageBuilder self() {
            return this;
        }
    }

}
