package com.sirius.sdk.agent.n_wise.messages;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONObject;

public class BaseNWiseMessage extends AriesProtocolMessage {
    public static final String PROTOCOL = "n-wise";

    public BaseNWiseMessage(String msg) {
        super(msg);
    }

    public static abstract class Builder<B extends BaseNWiseMessage.Builder<B>> extends AriesProtocolMessage.Builder<B> {

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            return super.generateJSON();
        }
    }
}
