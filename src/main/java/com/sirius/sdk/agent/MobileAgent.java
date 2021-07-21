package com.sirius.sdk.agent;

import com.sirius.sdk.agent.connections.AgentEvents;
import com.sirius.sdk.agent.coprotocols.*;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.agent.pairwise.WalletPairwiseList;
import com.sirius.sdk.agent.storages.InWalletImmutableCollection;
import com.sirius.sdk.agent.wallet.MobileWallet;
import com.sirius.sdk.base.WebSocketConnector;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.crypto.Crypto;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.hyperledger.indy.sdk.wallet.WalletExistsException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class MobileAgent extends AbstractAgent {

    JSONObject walletConfig = null;
    JSONObject walletCredentials = null;
    int timeoutSec = 60;
    String mediatorAddress;

    Wallet indyWallet;
    Map<String, WebSocketConnector> webSockets = new HashMap<>();

    class MobileAgentEvents implements AgentEvents {

        CompletableFuture<Message> future;
        @Override
        public CompletableFuture<Message> pull() {
            future = new CompletableFuture<>();
            return future;
        }
    }

    List<Pair<MobileAgentEvents, Listener>> events = new ArrayList<>();

    public MobileAgent(JSONObject walletConfig, JSONObject walletCredentials) {
        this.walletConfig = walletConfig;
        this.walletCredentials = walletCredentials;
    }

    @Override
    public void open() {
        try {
            Wallet.createWallet(walletConfig.toString(), walletCredentials.toString()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (!e.getMessage().contains("WalletExistsException"))
                e.printStackTrace();
        }
        try {
            this.indyWallet = Wallet.openWallet(walletConfig.toString(), walletCredentials.toString()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (!e.getMessage().contains("WalletAlreadyOpenedException"))
                e.printStackTrace();
        }
        wallet = new MobileWallet(indyWallet);
        pairwiseList = new WalletPairwiseList(wallet.getPairwise(), wallet.getDid());

        if (storage == null) {
            storage = new InWalletImmutableCollection(wallet.getNonSecrets());
        }

        for (String network : getNetworks()) {
            ledgers.put(network, new Ledger(network, wallet.getLedger(), wallet.getAnoncreds(), wallet.getCache(), storage));
        }
    }

    private List<String> getNetworks() {
        try {
            String str = Pool.listPools().get(timeoutSec, TimeUnit.SECONDS);
            JSONArray arr = new JSONArray(str);
            List<String> networks = new ArrayList<>();
            for (Object o : arr)
                networks.add(((JSONObject) o).optString("pool"));
            return networks;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isOpen() {
        return indyWallet != null;
    }

    @Override
    public String getName() {
        return "Mobile agent";
    }

    @Override
    public void sendMessage(Message message, List<String> their_vk, String endpoint, String my_vk, List<String> routing_keys) {
        if (!routing_keys.isEmpty())
            throw new RuntimeException("Not yet supported!");

        byte[] cryptoMsg = packMessage(message, my_vk,their_vk);

        if (endpoint.startsWith("http")) {
            try {
                HttpClient httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(endpoint);
                httpPost.setHeader("content-type", "application/ssi-agent-wire");
                httpPost.setEntity(new ByteArrayEntity(cryptoMsg));
                httpClient.execute(httpPost);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (endpoint.startsWith("ws")) {
            WebSocketConnector webSocket = getWebSocket(endpoint);
            if (!webSocket.isOpen())
                webSocket.open();
            webSocket.write(cryptoMsg);
        } else {
            throw new RuntimeException("Not yet supported!");
        }
    }

    WebSocketConnector getWebSocket(String endpoint) {
        if (webSockets.containsKey(endpoint)) {
            return webSockets.get(endpoint);
        } else {
            WebSocketConnector webSocket = new WebSocketConnector(endpoint, "", null);
            final MobileAgent fAgent = this;
            webSocket.readCallback = new Function<byte[], Void>() {
                @Override
                public Void apply(byte[] bytes) {
                    fAgent.receiveMsg(bytes);
                    return null;
                }
            };
            webSocket.open();
            webSockets.put(endpoint, webSocket);
            return webSocket;
        }
    }

    public void connect(String endpoint) {
        getWebSocket(endpoint);
    }

    public byte[] packMessage(Message msg,String myVk, List<String> theirVk) {
        JSONArray receivers = new JSONArray(theirVk.toArray());
        try {
            return Crypto.packMessage(
                    indyWallet, receivers.toString(),
                    myVk, msg.getMessageObj().toString().getBytes(StandardCharsets.UTF_8)).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void receiveMsg(byte[] bytes) {
        try {
            byte[] unpackedMessageBytes;
            JSONObject eventMessage;
            if (new JSONObject(new String(bytes)).has("protected")) {
                unpackedMessageBytes = Crypto.unpackMessage(this.indyWallet, bytes).get(timeoutSec, TimeUnit.SECONDS);
                JSONObject unpackedMessage = new JSONObject(new String(unpackedMessageBytes));
                eventMessage = new JSONObject().
                        put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/event").
                        put("content_type", "application/ssi-agent-wire").
                        put("@id", UUID.randomUUID()).
                        put("message", new JSONObject(unpackedMessage.optString("message"))).
                        put("recipient_verkey", unpackedMessage.optString("recipient_verkey"));
                if (unpackedMessage.has("sender_verkey")) {
                    eventMessage.put("sender_verkey", unpackedMessage.optString("sender_verkey"));
                }
            } else {
                unpackedMessageBytes = bytes;
                JSONObject unpackedMessage = new JSONObject(new String(unpackedMessageBytes));
                eventMessage = new JSONObject().
                        put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/event").
                        put("content_type", "application/ssi-agent-wire").
                        put("@id", UUID.randomUUID()).
                        put("message", unpackedMessage);
            }

            for (Pair<MobileAgentEvents, Listener> e : events)
                e.first.future.complete(new Message(eventMessage));
        } catch (InterruptedException | ExecutionException | TimeoutException | IndyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        for (Map.Entry<String, WebSocketConnector> ws : webSockets.entrySet()) {
            ws.getValue().close();
        }
        try {
            indyWallet.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean checkIsOpen() {
        return this.indyWallet != null;
    }

    @Override
    public Listener subscribe() {
        MobileAgentEvents e = new MobileAgentEvents();
        Listener listener = new Listener(e, this);
        events.add(new Pair<>(e, listener));
        return new Listener(e, this);
    }

    @Override
    public void unsubscribe(Listener listener) {
        for (Pair<MobileAgentEvents, Listener> e : events) {
            if (e.second == listener) {
                events.remove(e);
                break;
            }
        }
    }

    @Override
    public String generateQrCode(String value) {
        return null;
    }

    @Override
    public Pair<Boolean, List<String>> acquire(List<String> resources, Double lockTimeoutSec, Double enterTimeoutSec) {
        return null;
    }

    @Override
    public void release() {

    }

    @Override
    public AbstractCoProtocolTransport spawn(String my_verkey, TheirEndpoint endpoint) {
        return new TheirEndpointMobileCoProtocolTransport(this, my_verkey, endpoint);
    }

    @Override
    public AbstractCoProtocolTransport spawn(Pairwise pairwise) {
        return new PairwiseMobileCoProtocolTransport(this, pairwise);
    }

    @Override
    public AbstractCoProtocolTransport spawn(String thid, Pairwise pairwise) {
        return null;
    }

    @Override
    public AbstractCoProtocolTransport spawn(String thid) {
        return null;
    }

    @Override
    public AbstractCoProtocolTransport spawn(String thid, Pairwise pairwise, String pthid) {
        return null;
    }

    @Override
    public AbstractCoProtocolTransport spawn(String thid, String pthid) {
        return null;
    }
}
