package com.sirius.sdk.agent.n_wise.messages;

import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

public class LedgerUpdateNotify extends BaseNWiseMessage {

    static {
        Message.registerMessageClass(LedgerUpdateNotify.class, BaseNWiseMessage.PROTOCOL, "ledgerUpdateNotify");
    }

    public LedgerUpdateNotify(String msg) {
        super(msg);
    }

    public static Builder<?> builder() {
        return new LedgerUpdateNotifyBuilder();
    }

    public static abstract class Builder<B extends LedgerUpdateNotify.Builder<B>> extends BaseNWiseMessage.Builder<B> {

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            return jsonObject;
        }

        public LedgerUpdateNotify build() {
            return new LedgerUpdateNotify(generateJSON().toString());
        }
    }

    private static class LedgerUpdateNotifyBuilder extends LedgerUpdateNotify.Builder<LedgerUpdateNotifyBuilder> {
        @Override
        protected LedgerUpdateNotifyBuilder self() {
            return this;
        }
    }

}
