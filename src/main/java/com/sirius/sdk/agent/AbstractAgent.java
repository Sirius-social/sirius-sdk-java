package com.sirius.sdk.agent;

import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.microledgers.MicroledgerList;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.WalletPairwiseList;
import com.sirius.sdk.agent.wallet.AbstractWallet;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;
import com.sirius.sdk.utils.Pair;

import java.util.*;

public abstract class AbstractAgent extends TransportLayer {

    List<Endpoint> endpoints = new ArrayList<>();
    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    Map<String, Ledger> ledgers = new HashMap<>();
    WalletPairwiseList pairwiseList;
    AbstractWallet wallet;
    MicroledgerList microledgers;
    AbstractImmutableCollection storage;

    public abstract void open();

    public abstract boolean isOpen();

    public abstract String getName();

    /**
     * Implementation of basicmessage feature
     * See details:
     * - https://github.com/hyperledger/aries-rfcs/tree/master/features/0095-basic-message
     *
     * @param message      Message
     *                     See details:
     *                     - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0020-message-types
     * @param their_vk     Verkey of recipient
     * @param endpoint     Endpoint address of recipient
     * @param my_vk        VerKey of Sender (AuthCrypt mode)
     *                     See details:
     *                     - https://github.com/hyperledger/aries-rfcs/tree/master/features/0019-encryption-envelope#authcrypt-mode-vs-anoncrypt-mode
     * @param routing_keys Routing key of recipient
     * @return
     */
    public abstract void sendMessage(Message message, List<String> their_vk,
                                              String endpoint, String my_vk, List<String> routing_keys);

    public void sendTo(Message message, Pairwise to) {
        sendMessage(message, Collections.singletonList(to.getTheir().getVerkey()), to.getTheir().getEndpointAddress(), to.getMe().getVerkey(), to.getTheir().getRoutingKeys());
    }

    public abstract void close();

    public abstract boolean checkIsOpen();

    public abstract Listener subscribe();

    public abstract String generateQrCode(String value);

    public AbstractWallet getWallet() {
        checkIsOpen();
        return wallet;
    }

    public List<Endpoint> getEndpoints() {
        checkIsOpen();
        return endpoints;
    }

    public Map<String, Ledger> getLedgers() {
        checkIsOpen();
        return ledgers;
    }

    public AbstractMicroledgerList getMicroledgers() {
        checkIsOpen();
        return microledgers;
    }

    public WalletPairwiseList getPairwiseList() {
        checkIsOpen();
        return pairwiseList;
    }

    /**
     * Acquire N resources given by names
     * @param resources names of resources that you are going to lock
     * @param lockTimeoutSec max timeout resources will be locked. Resources will be automatically unlocked on expire
     * @param enterTimeoutSec timeout to wait resources are released
     * @return
     */
    public abstract Pair<Boolean, List<String>> acquire(List<String> resources, Double lockTimeoutSec, Double enterTimeoutSec);

    public Pair<Boolean, List<String>> acquire(List<String> resources, double lockTimeoutSec) {
        return acquire(resources, lockTimeoutSec, 3.0);
    }

    public abstract void release();

}
