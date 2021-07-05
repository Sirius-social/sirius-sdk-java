package com.sirius.sdk.hub;

import com.sirius.sdk.agent.AbstractAgent;
import com.sirius.sdk.agent.pairwise.AbstractPairwiseList;
import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.connections.BaseAgentConnection;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.wallet.abstract_wallet.*;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;

import java.io.Closeable;

public class CloudHub extends AbstractHub {

    public static class Config extends AbstractHub.Config {
        public String serverUri = null;
        public byte[] credentials;
        public P2PConnection p2p;
        public int ioTimeout = BaseAgentConnection.IO_TIMEOUT;
    }

    public CloudHub(CloudHub.Config config) {
        this.config = config;
        createAgentInstance();
    }

    public String getServerUri() {
        return ((CloudHub.Config)config).serverUri;
    }

    public CloudHub setServerUri(String serverUri) {
        ((CloudHub.Config)this.config).serverUri = serverUri;
        return this;
    }

    public byte[] getCredentials() {
        return ((CloudHub.Config)config).credentials;
    }

    public CloudHub setCredentials(byte[] credentials) {
        ((CloudHub.Config)this.config).credentials = credentials;
        return this;
    }

    public P2PConnection getConnection() {
        return ((CloudHub.Config)config).p2p;
    }

    public CloudHub setConnection(P2PConnection connection) {
        ((CloudHub.Config)this.config).p2p = connection;
        return this;
    }

    public int getTimeout() {
        return ((CloudHub.Config)config).ioTimeout;
    }

    public CloudHub setTimeout(int timeout) {
        ((CloudHub.Config)this.config).ioTimeout = timeout;
        return this;
    }

    @Override
    void createAgentInstance() {
        agent = new CloudAgent(
                ((CloudHub.Config)config).serverUri,
                ((CloudHub.Config)config).credentials,
                ((CloudHub.Config)config).p2p,
                ((CloudHub.Config)config).ioTimeout,
                config.storage);
        agent.open();
    }

}
