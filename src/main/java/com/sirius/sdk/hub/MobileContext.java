package com.sirius.sdk.hub;
import com.sirius.sdk.agent.BaseSender;

import org.json.JSONObject;

public class MobileContext extends Context {

    MobileContext(AbstractHub hub) {
        super(hub);
    }

    public MobileContext(MobileHub.Config config) {
        super(new MobileHub(config));
    }


    @Override
    public MobileHub getCurrentHub() {
        return (MobileHub) super.getCurrentHub();
    }

    public static class MobileContextBuilder {
        MobileHub.Config config = new MobileHub.Config();

        public MobileContextBuilder setWalletConfig(JSONObject walletConfig) {
            config.walletConfig = walletConfig;
            return this;
        }
        public MobileContextBuilder setIndyEndpoint(String indyEndpoint) {
            config.indyEndpoint = indyEndpoint;
            return this;
        }

        public MobileContextBuilder setServerUri(String serverUri) {
            config.serverUri = serverUri;
            return this;
        }

        public MobileContextBuilder setWalletCredentials(JSONObject walletCredentials) {
            config.walletCredentials = walletCredentials;
            return this;
        }

        public MobileContextBuilder setSender(BaseSender sender) {
            config.sender = sender;
            return this;
        }

        public Context build() {
            return new MobileContext(this.config);
        }
    }

    public static MobileContextBuilder builder() {
        return new MobileContext.MobileContextBuilder();
    }
}
