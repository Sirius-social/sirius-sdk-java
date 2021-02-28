package com.sirius.sdk.hub;

import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.BaseAgentConnection;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;

import java.io.Closeable;

public class Hub implements Closeable {

    String serverUri;
    byte[] credentials;
    P2PConnection connection;
    Agent agent = null;
    int timeout = BaseAgentConnection.IO_TIMEOUT;
    AbstractImmutableCollection storage;

    public Hub() {

    }

    public String getServerUri() {
        return serverUri;
    }

    public Hub setServerUri(String serverUri) {
        this.serverUri = serverUri;
        return this;
    }

    public byte[] getCredentials() {
        return credentials;
    }

    public Hub setCredentials(byte[] credentials) {
        this.credentials = credentials;
        return this;
    }

    public P2PConnection getConnection() {
        return connection;
    }

    public Hub setConnection(P2PConnection connection) {
        this.connection = connection;
        return this;
    }

    public Agent getAgent() {
        return agent;
    }

    public int getTimeout() {
        return timeout;
    }

    public Hub setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public AbstractImmutableCollection getStorage() {
        return storage;
    }

    public Hub setStorage(AbstractImmutableCollection storage) {
        this.storage = storage;
        return this;
    }

    void init() {
        if (agent == null) {
            agent = new Agent(serverUri, credentials, connection, timeout, storage);
        }
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
