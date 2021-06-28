package com.sirius.sdk.hub;

import com.sirius.sdk.agent.AbstractAgent;
import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.connections.BaseAgentConnection;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.pairwise.AbstractPairwiseList;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractAnonCreds;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCache;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractDID;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractNonSecrets;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;

import java.io.Closeable;

public abstract  class AbstractHub implements Closeable {

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
        public AbstractCache cache = null;
    }

    private final AbstractHub.Config config;
    AbstractAgent agent = null;

    public Config getConfig() {
        return config;
    }

    public AbstractHub(AbstractHub.Config config) {
        this.config = config;
        createAgentInstance();
    }

    public AbstractNonSecrets getNonSecrets() {
        if (this.config.nonSecrets != null) {
            return this.config.nonSecrets;
        } else {
            return agent.getWallet().getNonSecrets();
        }
    }

    public AbstractCrypto getCrypto() {
        if (this.config.crypto != null) {
            return this.config.crypto;
        } else {
            return agent.getWallet().getCrypto();
        }
    }

    public AbstractDID getDid() {
        if (this.config.did != null) {
            return this.config.did;
        } else {
            return getAgentConnectionLazy().getWallet().getDid();
        }
    }

    public AbstractPairwiseList getPairwiseList() {
        if (this.config.pairwiseStorage != null) {
            return this.config.pairwiseStorage;
        } else {
            return getAgentConnectionLazy().getPairwiseList();
        }
    }

    public String getServerUri() {
        return config.serverUri;
    }

    public AbstractHub setServerUri(String serverUri) {
        this.config.serverUri = serverUri;
        return this;
    }

    public byte[] getCredentials() {
        return config.credentials;
    }

    public AbstractHub setCredentials(byte[] credentials) {
        this.config.credentials = credentials;
        return this;
    }

    public P2PConnection getConnection() {
        return config.p2p;
    }

    public AbstractHub setConnection(P2PConnection connection) {
        this.config.p2p = connection;
        return this;
    }

    public AbstractAnonCreds getAnonCreds() {
        if (config.anoncreds != null) {
            return config.anoncreds;
        } else {
            return getAgentConnectionLazy().getWallet().getAnoncreds();
        }
    }

    public AbstractCache getCache() {
        if (config.cache != null) {
            return config.cache;
        } else {
            return getAgentConnectionLazy().getWallet().getCache();
        }
    }

    public AbstractAgent getAgent() {
        return agent;
    }

    public int getTimeout() {
        return config.ioTimeout;
    }

    public AbstractHub setTimeout(int timeout) {
        this.config.ioTimeout = timeout;
        return this;
    }

    public AbstractImmutableCollection getStorage() {
        return config.storage;
    }

    public AbstractMicroledgerList getMicroledgers() {
        if (config.microledgers != null) {
            return config.microledgers;
        } else {
            return getAgentConnectionLazy().getMicroledgers();
        }
    }

    public AbstractHub setStorage(AbstractImmutableCollection storage) {
        this.config.storage = storage;
        return this;
    }

    public AbstractAgent getAgentConnectionLazy() {
        if (!agent.isOpen()) {
            agent.open();
        }
        return agent;
    }

    public abstract void createAgentInstance();
}
