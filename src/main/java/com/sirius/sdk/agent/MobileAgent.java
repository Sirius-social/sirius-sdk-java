package com.sirius.sdk.agent;

import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.connections.AgentEvents;
import com.sirius.sdk.agent.coprotocols.*;
import com.sirius.sdk.agent.listener.Event;
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
import java.util.Arrays;
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

    public void setSender(BaseSender sender) {
        this.sender = sender;
    }

    BaseSender sender = new BaseSender() {
        WebSocketConnector webSocket;

        @Override
        public boolean sendTo(String endpoint, byte[] data) {
            if (endpoint.startsWith("http")) {
                HttpClient httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(endpoint);
                httpPost.setHeader("content-type", "application/ssi-agent-wire");
                httpPost.setEntity(new ByteArrayEntity(data));
                HttpResponse response = null;
                try {
                    response = httpClient.execute(httpPost);
                    int status = response.getStatusLine().getStatusCode();
                    return status == 200 || status == 202;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }else if(endpoint.startsWith("ws")){
                return webSocket.write(data);
            }
            return false;
        }

        @Override
        public void open() {
            webSocket.readCallback = new Function<byte[], Void>() {
                @Override
                public Void apply(byte[] bytes) {
                    receiveMsg(bytes);
                    return null;
                }
            };
            webSocket.open();
        }

        @Override
        public void close() {
            if (webSocket != null) {
                webSocket.close();
            }
        }

        @Override
        public void create() {
            webSocket = new WebSocketConnector(mediatorAddress, "", null);
        }
    };
    Wallet indyWallet;


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
        sender.open();

    }

    @Override
    public boolean isOpen() {
        return this.indyWallet!=null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void sendMessage(Message message, List<String> their_vk, String endpoint, String my_vk, List<String> routing_keys) {
        if (!routing_keys.isEmpty())
            throw new RuntimeException("Not yet supported!");

        byte[] cryptoMsg = null;
        try {
            JSONArray receivers = new JSONArray(their_vk.toArray());
            cryptoMsg = Crypto.packMessage(
                    indyWallet, receivers.toString(),
                    my_vk, message.getMessageObj().toString().getBytes(StandardCharsets.UTF_8)).get(timeoutSec, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

                if(sender!=null){
                  boolean isSend = sender.sendTo(endpoint,cryptoMsg);
                  //return new Pair<>(isSend, null);
                }
              /*  HttpClient httpClient = HttpClients.createDefault();
        if (endpoint.startsWith("http")) {
            try {
                HttpClient httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(endpoint);
                httpPost.setHeader("content-type", "application/ssi-agent-wire");
                httpPost.setEntity(new ByteArrayEntity(cryptoMsg));
                HttpResponse response = httpClient.execute(httpPost);
                int status = response.getStatusLine().getStatusCode();
                if (status == 200 || status == 202) {
                    return;
                }*/
           /* } catch (IOException e) {
                e.printStackTrace();
            }*/
       /* } else if (endpoint.startsWith("ws")) {
            webSocket.write(cryptoMsg);
            return;
        } else {
            throw new RuntimeException("Not yet supported!");
        }
        return;*/
    }

   /* public void sendMessageToMediator(Message message) {
        webSocket.write(message);
    }
*/
    public byte[] packMessage(Message msg, String theirVk) {
        JSONArray receivers = new JSONArray(new String[] {theirVk});
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

           /* try {
                JSONObject payload = new JSONObject(new String(data, StandardCharsets.US_ASCII));
                if (payload.has("protected")) {
                    String message = p2p.unpack(payload.toString());
                    return new Message(message);
                } else {

                    return new Message(payload.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();

            }*/
            Object message = null;
            String recipient_verkey = null;
            String sender_verkey = null;
            JSONObject payload = new JSONObject(new String(bytes, StandardCharsets.US_ASCII));
            if (payload.has("protected")) {
                byte[] unpackedMessageBytes = Crypto.unpackMessage(this.indyWallet, bytes).get(timeoutSec, TimeUnit.SECONDS);
                JSONObject unpackedMessage = new JSONObject(new String(unpackedMessageBytes));
                message = unpackedMessage.opt("message");
                recipient_verkey = unpackedMessage.optString("recipient_verkey");
                sender_verkey = unpackedMessage.optString("sender_verkey");
            } else {
                message = payload;
            }
            JSONObject messageObj = null;
            if(message!=null){
                if(message instanceof JSONObject){
                    messageObj = (JSONObject)message;
                }
                if(message instanceof String){
                    messageObj = new JSONObject((String)message);
                }
            }
            JSONObject eventMessage = new JSONObject().
                    put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/event").
                    put("content_type", "application/ssi-agent-wire").
                    put("@id", UUID.randomUUID()).
                    put("message", messageObj).
                    put("recipient_verkey",recipient_verkey );
            if (sender_verkey!=null) {
                eventMessage.put("sender_verkey", sender_verkey);
            }
            for (MobileAgentEvents e : events)
                e.future.complete(new Message(eventMessage));
        } catch (InterruptedException | ExecutionException | TimeoutException | IndyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        sender.close();
        if(checkIsOpen()){
            try {
                indyWallet.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (IndyException e) {
                e.printStackTrace();
            }
            indyWallet = null;
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
