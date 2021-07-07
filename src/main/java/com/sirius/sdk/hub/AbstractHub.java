package com.sirius.sdk.hub;

import com.sirius.sdk.agent.AbstractAgent;
import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.connections.BaseAgentConnection;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.microledgers.MicroledgerList;
import com.sirius.sdk.agent.pairwise.AbstractPairwiseList;
import com.sirius.sdk.agent.wallet.abstract_wallet.*;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;

import java.io.Closeable;

public abstract class AbstractHub implements Closeable {

    public static class Config {
        public AbstractCrypto crypto = null;
        public AbstractMicroledgerList microledgers = null;
        public AbstractPairwiseList pairwiseStorage = null;
        public AbstractDID did = null;
        public AbstractAnonCreds anoncreds = null;
        public AbstractNonSecrets nonSecrets = null;
        public AbstractImmutableCollection storage = null;
        public AbstractCache cache = null;
    }

    Config config;
    AbstractAgent agent = null;

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

    public AbstractMicroledgerList getMicroledgers() {
        return config.microledgers;
    }

    public AbstractAgent getAgentConnectionLazy() {
        if (!agent.isOpen()) {
            agent.open();
        }
        return agent;
    }

    public AbstractAgent getAgent() {
        return agent;
    }

    abstract void createAgentInstance();

    @Override
    public void close() {
        if (agent != null)
            agent.close();
    }

}
