package com.sirius.sdk.agent.aries_rfc.feature_0211_mediator_coordination_protocol;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONObject;

public class CoordinateMediationMessage extends AriesProtocolMessage {
    public static final String PROTOCOL = "coordinate-mediation";

    public CoordinateMediationMessage(String message) {
        super(message);
    }

    public static abstract class Builder<B extends CoordinateMediationMessage.Builder<B>> extends AriesProtocolMessage.Builder<B> {

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            setVersion("1.0");
            JSONObject jsonObject = super.generateJSON();
            return jsonObject;
        }
    }
}
