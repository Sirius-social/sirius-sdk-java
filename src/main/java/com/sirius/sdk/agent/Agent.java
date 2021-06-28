package com.sirius.sdk.agent;

import com.sirius.sdk.agent.connections.AgentEvents;
import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.connections.BaseAgentConnection;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.microledgers.AbstractMicroledgerList;
import com.sirius.sdk.agent.microledgers.MicroledgerList;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.coprotocols.PairwiseCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.TheirEndpointCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.agent.pairwise.WalletPairwiseList;
import com.sirius.sdk.agent.storages.InWalletImmutableCollection;
import com.sirius.sdk.agent.wallet.AbstractWallet;
import com.sirius.sdk.agent.wallet.DynamicWallet;
import com.sirius.sdk.agent.connections.RemoteCallWrapper;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.storage.abstract_storage.AbstractImmutableCollection;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.*;

/**
 * Agent connection in the self-sovereign identity ecosystem.
 * <p>
 * Managing an identity is complex. It is implementation of tools to help you to develop SSI Smart-Contracts logic.
 * See details:
 * - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0004-agents
 */
public class Agent extends AbstractAgent {

    AgentRPC rpc;
    AbstractImmutableCollection storage;

    /**
     * @param serverAddress example https://my-cloud-provider.com
     * @param credentials   credentials that point websocket connection to your agent and server-side services like
     *                      routing keys maintenance ant etc.
     * @param p2p           encrypted connection to establish tunnel to Agent that is running on server-side
     * @param timeout
     * @param storage
     * @param name
     */
    public Agent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage, String name) {
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
    public Agent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage) {
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
    public Agent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.p2p = p2p;
        this.timeout = timeout;
        this.name = null;
        this.storage = null;
    }

    @Override
    public DynamicWallet getWallet() {
        return (DynamicWallet)wallet;
    }

    public void open() {
        try {
            rpc = new AgentRPC(serverAddress, credentials, p2p, timeout);
            rpc.create();
            endpoints = rpc.getEndpoints();
            wallet = new DynamicWallet(rpc);
            if (storage == null) {
                storage = new InWalletImmutableCollection(wallet.getNonSecrets());
            }
            for (String network : rpc.getNetworks()) {
                ledgers.put(network, new Ledger(network, wallet.getLedger(), wallet.getAnoncreds(), wallet.getCache(), storage));
            }
            pairwiseList = new WalletPairwiseList(wallet.getPairwise(), wallet.getDid());
            microledgers = new MicroledgerList(rpc);
        } catch (SiriusFieldValueError siriusFieldValueError) {
            siriusFieldValueError.printStackTrace();
        }
    }

    public boolean isOpen() {
        return rpc != null && rpc.isOpen();
    }


    public boolean ping() {
        try {
            Object response = rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/ping_agent", null);
            if (response instanceof Boolean) {
                return (boolean) response;
            }
            return false;
        } catch (Exception siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return false;
    }

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
    public Pair<Boolean, Message> sendMessage(Message message, List<String> their_vk,
                                              String endpoint, String my_vk, List<String> routing_keys) throws SiriusRPCError {
        checkIsOpen();
        try {
            Message message1 = rpc.sendMessage(message, their_vk, endpoint, my_vk, routing_keys, false);
            return new Pair<>(true, message1);
        } catch (SiriusConnectionClosed siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        } catch (SiriusInvalidPayloadStructure siriusInvalidPayloadStructure) {
            siriusInvalidPayloadStructure.printStackTrace();
        }
        return new Pair<>(false, null);
    }

    public void sendTo(Message message, Pairwise to) throws SiriusRPCError {
        sendMessage(message, Collections.singletonList(to.getTheir().getVerkey()), to.getTheir().getEndpoint(), to.getMe().getVerkey(), to.getTheir().getRoutingKeys());
    }


    public void close() {
        if (rpc != null) {
            rpc.close();
        }
        if (events != null) {
            events.close();
        }
        wallet = null;
    }


    public List<Endpoint> checkIsOpen() {
        if (rpc != null) {
            if (rpc.isOpen()) {
                return rpc.getEndpoints();
            }
        }
        throw new RuntimeException("Open Agent at first!");
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

    public String generateQrCode(String value){
        checkIsOpen();
        RemoteParams params = RemoteParams.RemoteParamsBuilder.create()
                .add("value",value)
                .build();
        try {
            Object response =  rpc.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin/1.0/generate_qr",params);
            if(response instanceof JSONObject){
                JSONObject responseObject = (JSONObject) response;
                return  responseObject.getString("url");
            }
        } catch (Exception siriusConnectionClosed) {
            siriusConnectionClosed.printStackTrace();
        }
        return null;
    }

    @Override
    public TheirEndpointCoProtocolTransport spawn(String myVerkey, TheirEndpoint endpoint) {
        AgentRPC new_rpc = new AgentRPC(serverAddress, credentials, p2p, timeout);
        try {
            new_rpc.create();
            return new TheirEndpointCoProtocolTransport(myVerkey, endpoint, new_rpc);
        } catch (SiriusFieldValueError siriusFieldValueError) {
            siriusFieldValueError.printStackTrace();
        }
        return null;
    }

    @Override
    public PairwiseCoProtocolTransport spawn(Pairwise pairwise) {
        AgentRPC newRpc = new AgentRPC(serverAddress, credentials, p2p, timeout);
        try {
            newRpc.create();
            return new PairwiseCoProtocolTransport(pairwise, newRpc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid, Pairwise pairwise) {
        AgentRPC newRpc = new AgentRPC(serverAddress, credentials, p2p, timeout);
        try {
            newRpc.create();
            return new ThreadBasedCoProtocolTransport(thid, pairwise, newRpc, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid) {
        AgentRPC newRpc = new AgentRPC(serverAddress, credentials, p2p, timeout);
        try {
            newRpc.create();
            return new ThreadBasedCoProtocolTransport(thid, null, newRpc, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid, Pairwise pairwise, String pthid) {
        AgentRPC newRpc = new AgentRPC(serverAddress, credentials, p2p, timeout);
        try {
            newRpc.create();
            return new ThreadBasedCoProtocolTransport(thid, pairwise, newRpc, pthid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid, String pthid) {
        AgentRPC newRpc = new AgentRPC(serverAddress, credentials, p2p, timeout);
        try {
            newRpc.create();
            return new ThreadBasedCoProtocolTransport(thid, null, newRpc, pthid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Acquire N resources given by names
     * @param resources names of resources that you are going to lock
     * @param lockTimeoutSec max timeout resources will be locked. Resources will be automatically unlocked on expire
     * @param enterTimeoutSec timeout to wait resources are released
     * @return
     */
    public Pair<Boolean, List<String>> acquire(List<String> resources, Double lockTimeoutSec, Double enterTimeoutSec) {
        checkIsOpen();
        return new RemoteCallWrapper<Pair<Boolean, List<String>>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin/1.0/acquire",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("names", resources)
                                .add("enter_timeout", enterTimeoutSec)
                                .add("lock_timeout", lockTimeoutSec));
    }

    public Pair<Boolean, List<String>> acquire(List<String> resources, double lockTimeoutSec) {
        return acquire(resources, lockTimeoutSec, 3.0);
    }

    public void release() {
        new RemoteCallWrapper<Void>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin/1.0/release");
    }
}


