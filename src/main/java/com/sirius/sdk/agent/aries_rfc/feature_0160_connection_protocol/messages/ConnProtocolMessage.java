package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONObject;

public class ConnProtocolMessage extends AriesProtocolMessage {
    public static final String PROTOCOL = "connections";

    public ConnProtocolMessage(String msg) {
        super(msg);
    }

    public static abstract class Builder<B extends ConnProtocolMessage.Builder<B>> extends AriesProtocolMessage.Builder<B> {

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }
    }

    public static JSONObject buildDidDoc(String did, String verkey, String endpoint) {
        return null;
    }


}
