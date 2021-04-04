package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.aries_rfc.Utils;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Message to commit transactions list
 */
public class CommitTransactionsMessage extends BaseTransactionsMessage {

    static {
        Message.registerMessageClass(CommitTransactionsMessage.class, SimpleConsensusMessage.PROTOCOL, "stage-commit");
    }

    public CommitTransactionsMessage(String msg) {
        super(msg);
    }

    public JSONObject getPreCommits() {
        JSONObject preCommits = getMessageObj().optJSONObject("pre_commits");
        return preCommits != null ? preCommits : new JSONObject();
    }

    public void addPreCommit(String participant, PreCommitTransactionsMessage preCommit) {
        if (!preCommit.getMessageObj().has("hash~sig")) {
            new SiriusContextError("Pre-Commit for participant" + participant + "does not have hash~sig attribute").printStackTrace();
            return;
        }
        JSONObject preCommits = this.getPreCommits();
        preCommits.put(participant, preCommit.getMessageObj().get("hash~sig"));
        getMessageObj().put("pre_commits", preCommits);
    }

    @Override
    public void validate() throws SiriusValidationError {
        super.validate();
        for (String participant : this.getParticipants()) {
            if (!this.getPreCommits().has(participant)) {
                throw new SiriusValidationError("Pre-Commit for participant" + participant + "does not exists");
            }
        }
    }

    public JSONObject verifyPreCommits(AbstractCrypto api, MicroLedgerState expectedState) throws SiriusValidationError {
        JSONObject states = new JSONObject();
        JSONObject preCommits = this.getPreCommits();
        for (String participant : preCommits.keySet()) {
            JSONObject signed = preCommits.optJSONObject(participant);
            Pair<String, Boolean> verSignRes = Utils.verifySigned(api, signed);
            if (!verSignRes.second) {
                throw new SiriusValidationError("Error verifying pre_commit for participant: " + participant);
            }
            if (!verSignRes.first.equals(expectedState.getHash())) {
                throw new SiriusValidationError("Ledger state for participant " + participant + "is not consistent");
            }
            states.put(participant, new JSONArray().put(expectedState).put(signed));
        }
        return states;
    }

    public static Builder<?> builder() {
        return new CommitTransactionsMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends BaseTransactionsMessage.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public CommitTransactionsMessage build() {
            return new CommitTransactionsMessage(generateJSON().toString());
        }
    }

    private static class CommitTransactionsMessageBuilder extends Builder<CommitTransactionsMessageBuilder> {
        @Override
        protected CommitTransactionsMessageBuilder self() {
            return this;
        }
    }
}
