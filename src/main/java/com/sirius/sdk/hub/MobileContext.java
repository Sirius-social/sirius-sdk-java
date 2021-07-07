package com.sirius.sdk.hub;
import org.json.JSONObject;

public class MobileContext extends Context {

    MobileContext(AbstractHub hub) {
        super(hub);
    }

    public MobileContext(MobileHub.Config config) {
        super(new MobileHub(config));
    }

    public static class MobileContextBuilder {
        MobileHub.Config config = new MobileHub.Config();

        public MobileContextBuilder setWalletConfig(JSONObject walletConfig) {
            config.walletConfig = walletConfig;
            return this;
        }

        public MobileContextBuilder setWalletCredentials(JSONObject walletCredentials) {
            config.walletCredentials = walletCredentials;
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
