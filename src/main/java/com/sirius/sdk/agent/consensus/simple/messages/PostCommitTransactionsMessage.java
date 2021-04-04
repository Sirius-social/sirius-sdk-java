package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.aries_rfc.Utils;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Message to commit transactions list
 */
public class PostCommitTransactionsMessage extends BaseTransactionsMessage {

    static {
        Message.registerMessageClass(PostCommitTransactionsMessage.class, SimpleConsensusMessage.PROTOCOL, "stage-post-commit");
    }

    public PostCommitTransactionsMessage(String msg) {
        super(msg);
    }

    public JSONArray getCommits() {
        JSONArray commits = getMessageObj().optJSONArray("commits");
        return commits != null ? commits : new JSONArray();
    }


    public void addCommitSign(AbstractCrypto api, CommitTransactionsMessage commit, Pairwise.Me me) {
        JSONObject signed = Utils.sign(api, commit.getMessageObj(), me.getVerkey());
        JSONArray commits = getCommits();
        commits.put(signed);
        getMessageObj().put("commits", commits);
    }

    public boolean verifyCommits(AbstractCrypto api, CommitTransactionsMessage expected, List<String> verkeys) {
        List<String> actualVerkeys = new ArrayList<>();
        for (Object o : getCommits()) {
            actualVerkeys.add(((JSONObject) o).getString("signer"));
        }
        if (!new HashSet<String>(actualVerkeys).containsAll(verkeys)) {
            return false;
        }

        for (Object signed : getCommits()) {
            Pair<String, Boolean> t1 = Utils.verifySigned(api, (JSONObject) signed);
            if (t1.second) {
                JSONObject commit = new JSONObject(t1.first);
                JSONObject cleanedCommit = new JSONObject();
                for (String key : commit.keySet()) {
                    if (!key.startsWith("~"))
                        cleanedCommit.put(key, commit.get(key));
                }
                JSONObject cleanedExpect = new JSONObject();
                for (String key : expected.getMessageObj().keySet()) {
                    if (!key.startsWith("~"))
                        cleanedExpect.put(key, expected.getMessageObj().get(key));
                }
                if (!cleanedCommit.similar(cleanedExpect))
                    return false;
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public void validate() throws SiriusValidationError {
        super.validate();
        if (this.getCommits().isEmpty()) {
            throw new SiriusValidationError("Commits collection is empty");
        }
    }

    public static Builder<?> builder() {
        return new PostCommitTransactionsMessageBuilder();
    }

    public static abstract class Builder<B extends Builder<B>> extends BaseTransactionsMessage.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }

        public PostCommitTransactionsMessage build() {
            return new PostCommitTransactionsMessage(generateJSON().toString());
        }
    }

    private static class PostCommitTransactionsMessageBuilder extends Builder<PostCommitTransactionsMessageBuilder> {
        @Override
        protected PostCommitTransactionsMessageBuilder self() {
            return this;
        }
    }
}
