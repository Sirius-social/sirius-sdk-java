package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.n_wise.messages.*;
import com.sirius.sdk.agent.n_wise.transactions.GenesisTx;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2PAnon;
import com.sirius.sdk.utils.IotaUtils;
import com.sirius.sdk.utils.Pair;
import org.bitcoinj.core.Base58;
import org.iota.client.Client;
import org.iota.client.MessageId;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.sirius.sdk.utils.IotaUtils.generateTag;

public class IotaChat {

    public static final String MAINNET = "https://chrysalis-nodes.iota.cafe:443";
    public static final String TESTNET = "https://api.lb-0.h.chrysalis-devnet.iota.cafe";

    public static int timeToLiveSec = 60;

    String chatName = null;

    private static IotaChat acceptInvitation() {
        return null;
    }

    private IotaChat() {

    }

    private IotaChat(GenesisTx initialMessage) {
        this.chatName = initialMessage.getLabel();
    }

    public static IotaChat createChat(String chatName, String myNickName, Context context) {
        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        GenesisTx initialMessage = GenesisTx.builder().
                setLabel(chatName).
                setCreatorNickName(myNickName).
                setCreatorDid(didVk.first).
                setCreatorVerkey(Base58.decode(didVk.second)).setCreatorEndpoint(context.getEndpointAddressWithEmptyRoutingKeys())
                .build();

        Client iota = node();
        try {
            String tag = generateTag(initialMessage.getId().getBytes());
            iota.message().
                    withIndexString(tag).
                    withData(initialMessage.toString().getBytes(StandardCharsets.UTF_8)).
                    finish();

            return new IotaChat(initialMessage);
        } catch (Exception ex) {
            return null;
        }
    }

    public static IotaChat accept(Invitation invitation, String nickName, Context context) {
        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        TheirEndpoint inviterEndpoint = new TheirEndpoint(invitation.getEndpoint(), invitation.getInviterVerkey(), invitation.routingKeys());
        List<String> protocols = Arrays.asList(BaseNWiseMessage.PROTOCOL, Ack.PROTOCOL, Ping.PROTOCOL);
        try (AbstractP2PCoProtocol cp = new CoProtocolP2PAnon(context, didVk.second, inviterEndpoint, protocols, timeToLiveSec)) {
            Request request = Request.builder().
                    setNickname(nickName).
                    setDid(didVk.first).
                    setVerkey(didVk.second).
                    setEndpoint(context.getEndpointAddressWithEmptyRoutingKeys()).
                    build();

            Pair<Boolean, com.sirius.sdk.messaging.Message> okMsg = cp.sendAndWait(request);
            if (okMsg.first && okMsg.second instanceof Response) {
                Response response = (Response) okMsg.second;
                IotaResponseAttach attach = new IotaResponseAttach(response.getAttach());
                String tag = attach.getTag();
                MessageId[] fetchedMessageIds = IotaUtils.node().getMessage().indexString(tag);
                for (MessageId msgId : fetchedMessageIds) {
                    org.iota.client.Message msg = IotaUtils.node().getMessage().data(msgId);

                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new IotaChat();
    }

    public boolean accept(Request request, Context context) {

        return false;
    }

    public Invitation createInvitation(Context context) {
        return Invitation.builder().
                setLabel(chatName).
                setInviterKey(context.getCrypto().createKey()).
                setEndpoint(context.getEndpointAddressWithEmptyRoutingKeys()).
                build();
    }

    public boolean send(Message message) {

        return false;
    }

    public String myKey() {
        return null;
    }

    public String resolveNickName(String verkey) {
        return null;
    }

    public List<Pairwise.Their> getParticipants() {
        return null;
    }

    public boolean fetchFromLedger() {
        return false;
    }

    private static Client node() {
        return Client.Builder().withNode(IotaUtils.iotaNetwork).finish();
    }

}
