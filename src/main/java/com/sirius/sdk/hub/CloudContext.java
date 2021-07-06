package com.sirius.sdk.hub;

import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.encryption.P2PConnection;

public class CloudContext extends Context {

    CloudContext(AbstractHub hub) {
        super(hub);
    }

    public CloudContext(CloudHub.Config config) {
        super(new CloudHub(config));
    }

    public static class CloudContextBuilder {
        CloudHub.Config config = new CloudHub.Config();

        public CloudContextBuilder setCrypto(AbstractCrypto crypto) {
            this.config.crypto = crypto;
            return this;
        }

        public CloudContextBuilder setMicroledgers(AbstractMicroledgerList microledgers) {
            this.config.microledgers = microledgers;
            return this;
        }

        public CloudContextBuilder setServerUri(String serverUri) {
            this.config.serverUri = serverUri;
            return this;
        }

        public CloudContextBuilder setCredentials(byte[] credentials) {
            this.config.credentials = credentials;
            return this;
        }

        public CloudContextBuilder setP2p(P2PConnection p2p) {
            this.config.p2p = p2p;
            return this;
        }

        public CloudContextBuilder setTimeoutSec(int timeoutSec) {
            this.config.ioTimeout = timeoutSec;
            return this;
        }

        public Context build() {
            return new CloudContext(this.config);
        }
    }

    public static CloudContextBuilder builder() {
        return new CloudContextBuilder();
    }
}
