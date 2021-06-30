package com.sirius.sdk.agent;

import com.sirius.sdk.agent.coprotocols.PairwiseCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.TheirEndpointCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MobileAgent extends AbstractAgent {

    JSONObject walletConfig = null;
    JSONObject walletCredentials = null;
    int timeoutSec = 60;

    Wallet wallet;

    public MobileAgent(JSONObject walletConfig, JSONObject walletCredentials) {
        this.walletConfig = walletConfig;
        this.walletCredentials = walletCredentials;
    }

    @Override
    public void open() {
        try {
            //Wallet.createWallet(walletConfig.toString(), walletCredentials.toString()).get(timeoutSec, TimeUnit.SECONDS);
            this.wallet = Wallet.openWallet(walletConfig.toString(), walletCredentials.toString()).get(timeoutSec, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IndyException e) {
            e.printStackTrace();
        }
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
    public Pair<Boolean, Message> sendMessage(Message message, List<String> their_vk, String endpoint, String my_vk, List<String> routing_keys) {
        if (!routing_keys.isEmpty())
            throw new RuntimeException("Not yet supported!");

        if (endpoint.startsWith("http")) {
            JSONArray receivers = new JSONArray(their_vk.toArray());
            try {
                byte[] cryptoMsg = Crypto.packMessage(
                        wallet, receivers.toString(),
                        my_vk, message.getMessageObj().toString().getBytes(StandardCharsets.UTF_8)).get(timeoutSec, TimeUnit.SECONDS);
                HttpClient httpClient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(endpoint);
                httpPost.setHeader("content-type", "application/ssi-agent-wire");
                httpPost.setEntity(new ByteArrayEntity(cryptoMsg));
                HttpResponse response = httpClient.execute(httpPost);
                int status = response.getStatusLine().getStatusCode();
                if (status == 200 || status == 202) {
                    return new Pair<>(true, null);
                }
            } catch (IndyException | InterruptedException | ExecutionException | TimeoutException | IOException e) {
                e.printStackTrace();
            }
        }
        return new Pair<>(false, null);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean checkIsOpen() {
        return false;
    }

    @Override
    public Listener subscribe() {
        return null;
    }

    @Override
    public String generateQrCode(String value) {
        return null;
    }

    @Override
    public TheirEndpointCoProtocolTransport spawn(String my_verkey, TheirEndpoint endpoint) {
        return null;
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
