package com.sirius.sdk.hub;

import com.sirius.sdk.agent.AbstractPairwiseList;
import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.BaseAgentConnection;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractAnonCreds;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractDID;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;

import java.io.Closeable;

public class Hub implements Closeable {

    public static class Config {
        public AbstractCrypto crypto = null;
        public AbstractMicroledgerList microledgers = null;
        public AbstractPairwiseList pairwiseStorage = null;
        public AbstractDID did = null;
        public AbstractAnonCreds anoncreds = null;
        public AbstractNonSecrets nonSecrets = null;
        public String serverUri = null;
        public byte[] credentials;
        public P2PConnection p2p;
        public int ioTimeout = BaseAgentConnection.IO_TIMEOUT;
        public AbstractImmutableCollection storage = null;
    }

    Config config;
    Agent agent = null;

    public Hub(Config config) {
        this.config = config;
    }

    public String getServerUri() {
        return config.serverUri;
    }

    public Hub setServerUri(String serverUri) {
        this.config.serverUri = serverUri;
        return this;
    }

    public byte[] getCredentials() {
        return config.credentials;
    }

    public Hub setCredentials(byte[] credentials) {
        this.config.credentials = credentials;
        return this;
    }

    public P2PConnection getConnection() {
        return config.p2p;
    }

    public Hub setConnection(P2PConnection connection) {
        this.config.p2p = connection;
        return this;
    }

    public Agent getAgent() {
        return agent;
    }

    public int getTimeout() {
        return config.ioTimeout;
    }

    public Hub setTimeout(int timeout) {
        this.config.ioTimeout = timeout;
        return this;
    }

    public AbstractImmutableCollection getStorage() {
        return config.storage;
    }

    public Hub setStorage(AbstractImmutableCollection storage) {
        this.config.storage = storage;
        return this;
    }

    void createAgentInstance() {
        agent = new Agent(config.serverUri, config.credentials, config.p2p, config.ioTimeout, config.storage);
    }

    @Override
    public void close() {
        if (agent != null)
            agent.close();
    }

    public void open() {
        if (agent != null) {
            agent.open();
        }
    }
}
