package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.aries_rfc.Utils;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

/**
 * Message to accumulate participants signed accepts for transactions list
 */
public class PreCommitTransactionsMessage extends BaseTransactionsMessage {

    static {
        Message.registerMessageClass(PreCommitTransactionsMessage.class, SimpleConsensusMessage.PROTOCOL, "stage-pre-commit");
    }

    public void signState(AbstractCrypto api, Pairwise.Me me) {
        JSONObject signed = Utils.sign(api, getHash(), me.getVerkey());
        getMessageObj().put("hash~sig", signed);
        getMessageObj().remove("state");
    }

    public Pair<Boolean, String> verifyState(AbstractCrypto api, String expectedVerkey) {
        JSONObject hashSigned = getMessageObj().optJSONObject("hash~sig");
        if (hashSigned != null) {
            if(hashSigned.optString("signer").equals(expectedVerkey)) {
                Pair<JSONObject, Boolean> verSigRes = Utils.verifySigned(api, hashSigned);
                return new Pair<>(verSigRes.second, verSigRes.first.toString());
            }
        }
        return new Pair<>(false, null);
    }

    public PreCommitTransactionsMessage(String msg) {
        super(msg);
    }

    public static Builder<?> builder() {
        return new PreCommitTransactionsMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends BaseTransactionsMessage.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public PreCommitTransactionsMessage build() {
            return new PreCommitTransactionsMessage(generateJSON().toString());
        }
    }

    private static class PreCommitTransactionsMessageBuilder extends Builder<PreCommitTransactionsMessageBuilder> {
        @Override
        protected PreCommitTransactionsMessageBuilder self() {
            return this;
        }
    }
}
