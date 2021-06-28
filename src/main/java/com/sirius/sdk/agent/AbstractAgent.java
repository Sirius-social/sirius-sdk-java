package com.sirius.sdk.agent;

import com.sirius.sdk.agent.connections.AgentEvents;
import com.sirius.sdk.agent.connections.BaseAgentConnection;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.connections.RemoteCallWrapper;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.microledgers.MicroledgerList;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.WalletPairwiseList;
import com.sirius.sdk.agent.wallet.AbstractWallet;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusFieldValueError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusRPCError;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;
import com.sirius.sdk.utils.Pair;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract  class AbstractAgent extends TransportLayer {

    public abstract boolean isOpen();

    public abstract void open();

    public abstract void close();

    public abstract List<Endpoint> checkIsOpen();
    public abstract String generateQrCode(String value);
    public abstract boolean ping();



    AbstractWallet wallet;

    public void setMicroledgers(MicroledgerList microledgers) {
        this.microledgers = microledgers;
    }

    MicroledgerList microledgers;

    public void setPairwiseList(WalletPairwiseList pairwiseList) {
        this.pairwiseList = pairwiseList;
    }

    WalletPairwiseList pairwiseList;

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    List<Endpoint> endpoints;
    Map<String, Ledger> ledgers = new HashMap<>();
    AgentEvents events;

    public String getServerAddress() {
        return serverAddress;
    }

    String serverAddress;

    public byte[] getCredentials() {
        return credentials;
    }

    byte[] credentials;

    public P2PConnection getP2p() {
        return p2p;
    }

    P2PConnection p2p;

    public int getTimeout() {
        return timeout;
    }

    int timeout = BaseAgentConnection.IO_TIMEOUT;
    String name;

    public AbstractImmutableCollection getStorage() {
        return storage;
    }

    public void setStorage(AbstractImmutableCollection storage) {
        this.storage = storage;
    }

    AbstractImmutableCollection storage;

    public void setWallet(AbstractWallet wallet) {
        this.wallet = wallet;
    }

    /**
     * @param serverAddress example https://my-cloud-provider.com
     * @param credentials   credentials that point websocket connection to your agent and server-side services like
     *                      routing keys maintenance ant etc.
     * @param p2p           encrypted connection to establish tunnel to Agent that is running on server-side
     * @param timeout
     * @param storage
     * @param name
     */
    public AbstractAgent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage, String name) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.p2p = p2p;
        this.timeout = timeout;
        this.name = name;
        this.storage = storage;
    }

    /**
     *Overload constructor {@link #Agent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage, String name)}
     */
    public AbstractAgent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.p2p = p2p;
        this.timeout = timeout;
        this.name = null;
        this.storage = storage;
    }

    /**
     *Overload constructor {@link #Agent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage, String name)}
     */
    public AbstractAgent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.p2p = p2p;
        this.timeout = timeout;
        this.name = null;
        this.storage = null;
    }


    public String getName() {
        return name;
    }


    public Map<String, Ledger> getLedgers() {
        checkIsOpen();
        return ledgers;
    }


    public WalletPairwiseList getPairwiseList() {
        checkIsOpen();
        return pairwiseList;
    }

    public List<Endpoint> getEndpoints() {
        checkIsOpen();
        return endpoints;
    }

    public AbstractWallet getWallet() {
        checkIsOpen();
        return wallet;
    }

    public AbstractMicroledgerList getMicroledgers() {
        checkIsOpen();
        return microledgers;
    }

    public Listener subscribe() {
        checkIsOpen();
        events = new AgentEvents(serverAddress, credentials, p2p, timeout);
        try {
            events.create();
        } catch (SiriusFieldValueError siriusFieldValueError) {
            siriusFieldValueError.printStackTrace();
        }
        return new Listener(events, pairwiseList);

    }

    public abstract void sendTo(Message message, Pairwise to) throws SiriusRPCError;

    /**
     * Acquire N resources given by names
     * @param resources names of resources that you are going to lock
     * @param lockTimeoutSec max timeout resources will be locked. Resources will be automatically unlocked on expire
     * @param enterTimeoutSec timeout to wait resources are released
     * @return
     */
    public abstract Pair<Boolean, List<String>> acquire(List<String> resources, Double lockTimeoutSec, Double enterTimeoutSec);

    public abstract void release();
}
