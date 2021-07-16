package com.sirius.sdk.hub;

import com.sirius.sdk.agent.MobileAgent;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import org.json.JSONObject;

public class MobileHub extends AbstractHub {

    public static class Config extends AbstractHub.Config {
        public JSONObject walletConfig = null;
        public JSONObject walletCredentials = null;
        public Invitation mediatorInvitation = null;
    }

    public MobileHub(MobileHub.Config config) {
        this.config = config;
        createAgentInstance();
    }

    @Override
    void createAgentInstance() {
        Config mobileConfig = (Config) config;
        agent = new MobileAgent(
                mobileConfig.walletConfig,
                mobileConfig.walletCredentials,
                mobileConfig.mediatorInvitation.endpoint());
        agent.open();
    }
}
