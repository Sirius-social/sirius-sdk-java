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
    MicroledgerList microledgers;
    WalletPairwiseList pairwiseList;
    List<Endpoint> endpoints;
    Map<String, Ledger> ledgers = new HashMap<>();
    AgentEvents events;
    String serverAddress;
    byte[] credentials;
    P2PConnection p2p;
    int timeout = BaseAgentConnection.IO_TIMEOUT;
    String name;

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
