package com.sirius.sdk.agent;

import com.sirius.sdk.agent.connections.CloudAgentEvents;
import com.sirius.sdk.agent.connections.AgentRPC;
import com.sirius.sdk.agent.connections.BaseAgentConnection;
import com.sirius.sdk.agent.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.microledgers.MicroledgerList;
import com.sirius.sdk.agent.coprotocols.PairwiseCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.TheirEndpointCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.agent.pairwise.WalletPairwiseList;
import com.sirius.sdk.agent.storages.InWalletImmutableCollection;
import com.sirius.sdk.agent.wallet.CloudWallet;
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
public class CloudAgent extends AbstractAgent {

    String serverAddress;
    byte[] credentials;
    P2PConnection p2p;
    int timeout = BaseAgentConnection.IO_TIMEOUT;
    String name;

    AgentRPC rpc;

    CloudAgentEvents events;

    /**
     * @param serverAddress example https://my-cloud-provider.com
     * @param credentials   credentials that point websocket connection to your agent and server-side services like
     *                      routing keys maintenance ant etc.
     * @param p2p           encrypted connection to establish tunnel to Agent that is running on server-side
     * @param timeout
     * @param storage
     * @param name
     */
    public CloudAgent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage, String name) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.p2p = p2p;
        this.timeout = timeout;
        this.name = name;
        this.storage = storage;
    }

    /**
     *Overload constructor {@link #CloudAgent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage, String name)}
     */
    public CloudAgent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.p2p = p2p;
        this.timeout = timeout;
        this.name = null;
        this.storage = storage;
    }

    /**
     *Overload constructor {@link #CloudAgent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout, AbstractImmutableCollection storage, String name)}
     */
    public CloudAgent(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.p2p = p2p;
        this.timeout = timeout;
        this.name = null;
        this.storage = null;
    }

    @Override
    public void open() {
        try {
            rpc = new AgentRPC(serverAddress, credentials, p2p, timeout);
            rpc.create();
            endpoints = rpc.getEndpoints();
            wallet = new CloudWallet(rpc);
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

    @Override
    public boolean isOpen() {
        return rpc != null && rpc.isOpen();
    }

    @Override
    public String getName() {
        return name;
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
    @Override
    public void sendMessage(Message message, List<String> their_vk,
                                              String endpoint, String my_vk, List<String> routing_keys) {
        checkIsOpen();
        try {
            rpc.sendMessage(message, their_vk, endpoint, my_vk, routing_keys, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public boolean checkIsOpen() {
        if (rpc != null) {
            return true;
        }
        throw new RuntimeException("Open Agent at first!");
    }

    @Override
    public Listener subscribe() {
        checkIsOpen();
        events = new CloudAgentEvents(serverAddress, credentials, p2p, timeout);
        try {
            events.create();
        } catch (SiriusFieldValueError siriusFieldValueError) {
            siriusFieldValueError.printStackTrace();
        }
        return new Listener(events, pairwiseList);

    }

    @Override
    public String generateQrCode(String value) {
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
    public AbstractCoProtocolTransport spawn(String myVerkey, TheirEndpoint endpoint) {
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
    @Override
    public Pair<Boolean, List<String>> acquire(List<String> resources, Double lockTimeoutSec, Double enterTimeoutSec) {
        checkIsOpen();
        return new RemoteCallWrapper<Pair<Boolean, List<String>>>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin/1.0/acquire",
                        RemoteParams.RemoteParamsBuilder.create()
                                .add("names", resources)
                                .add("enter_timeout", enterTimeoutSec)
                                .add("lock_timeout", lockTimeoutSec));
    }

    @Override
    public void release() {
        new RemoteCallWrapper<Void>(rpc){}.
                remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/admin/1.0/release");
    }

    public CloudAgentEvents getEvents() {
        checkIsOpen();
        return events;
    }
}


