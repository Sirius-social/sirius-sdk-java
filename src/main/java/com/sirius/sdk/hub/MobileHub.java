package com.sirius.sdk.hub;

import com.sirius.sdk.agent.MobileAgent;
import org.json.JSONObject;

public class MobileHub extends AbstractHub {

    public static class Config extends AbstractHub.Config {
        public JSONObject walletConfig = null;
        public JSONObject walletCredentials = null;
    }

    public MobileHub(MobileHub.Config config) {
        this.config = config;
        createAgentInstance();
    }

    @Override
    void createAgentInstance() {
        agent = new MobileAgent(((Config) config).walletConfig, ((Config) config).walletCredentials);
        agent.open();
    }
}
