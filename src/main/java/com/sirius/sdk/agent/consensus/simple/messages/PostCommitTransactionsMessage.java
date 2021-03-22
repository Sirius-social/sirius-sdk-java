package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

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
