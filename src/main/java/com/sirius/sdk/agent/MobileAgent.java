package com.sirius.sdk.agent;

import com.sirius.sdk.agent.connections.AgentEvents;
import com.sirius.sdk.agent.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.PairwiseCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.TheirEndpointMobileCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.agent.pairwise.WalletPairwiseList;
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
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    WebSocketConnector webSocket;

    class MobileAgentEvents implements AgentEvents {

        CompletableFuture<Message> future;
        @Override
        public CompletableFuture<Message> pull() {
            future = new CompletableFuture<>();
            return future;
        }
    }

    List<MobileAgentEvents> events = new ArrayList<>();

    public MobileAgent(JSONObject walletConfig, JSONObject walletCredentials, String mediatorAddress) {
        this.walletConfig = walletConfig;
        this.walletCredentials = walletCredentials;
        this.mediatorAddress = mediatorAddress;
        webSocket = new WebSocketConnector(this.mediatorAddress, "", null);
    }

    @Override
    public void open() {
        try {
            Wallet.createWallet(walletConfig.toString(), walletCredentials.toString()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException | IndyException e) {
            e.printStackTrace();
        }
        try {
            this.indyWallet = Wallet.openWallet(walletConfig.toString(), walletCredentials.toString()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException | IndyException e) {
            e.printStackTrace();
        }
        wallet = new MobileWallet(indyWallet);
        pairwiseList = new WalletPairwiseList(wallet.getPairwise(), wallet.getDid());

        final MobileAgent fAgent = this;
        webSocket.readCallback = new Function<byte[], Void>() {
            @Override
            public Void apply(byte[] bytes) {
                fAgent.receiveMsg(bytes);
                return null;
            }
        };
        webSocket.open();
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void sendMessage(Message message, List<String> their_vk, String endpoint, String my_vk, List<String> routing_keys) {
        if (!routing_keys.isEmpty())
            throw new RuntimeException("Not yet supported!");

        byte[] cryptoMsg = packMessage(message, their_vk);

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
            webSocket.write(cryptoMsg);
        } else {
            throw new RuntimeException("Not yet supported!");
        }
    }

    public byte[] packMessage(Message msg, List<String> theirVk) {
        JSONArray receivers = new JSONArray(theirVk.toArray());
        try {
            return Crypto.packMessage(
                    indyWallet, receivers.toString(),
                    null, msg.getMessageObj().toString().getBytes(StandardCharsets.UTF_8)).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void receiveMsg(byte[] bytes) {
        try {
            byte[] unpackedMessageBytes = Crypto.unpackMessage(this.indyWallet, bytes).get(timeoutSec, TimeUnit.SECONDS);
            JSONObject unpackedMessage = new JSONObject(new String(unpackedMessageBytes));
            JSONObject eventMessage = new JSONObject().
                    put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/event").
                    put("content_type", "application/ssi-agent-wire").
                    put("@id", UUID.randomUUID()).
                    put("message", unpackedMessage.optJSONObject("message")).
                    put("recipient_verkey", unpackedMessage.optString("recipient_verkey"));
            if (unpackedMessage.has("sender_verkey")) {
                eventMessage.put("sender_verkey", unpackedMessage.optString("sender_verkey"));
            }
            for (MobileAgentEvents e : events)
                e.future.complete(new Message(eventMessage));
        } catch (InterruptedException | ExecutionException | TimeoutException | IndyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (webSocket != null) {
            webSocket.close();
        }
    }

    @Override
    public boolean checkIsOpen() {
        return this.indyWallet != null;
    }

    @Override
    public Listener subscribe() {
        MobileAgentEvents e = new MobileAgentEvents();
        events.add(e);
        return new Listener(e, pairwiseList);
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
    public PairwiseCoProtocolTransport spawn(Pairwise pairwise) {
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid, Pairwise pairwise) {
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid) {
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid, Pairwise pairwise, String pthid) {
        return null;
    }

    @Override
    public ThreadBasedCoProtocolTransport spawn(String thid, String pthid) {
        return null;
    }
}
