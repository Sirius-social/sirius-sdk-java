package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.n_wise.messages.*;
import com.sirius.sdk.agent.n_wise.transactions.AddParticipantTx;
import com.sirius.sdk.agent.n_wise.transactions.GenesisTx;
import com.sirius.sdk.agent.n_wise.transactions.NWiseTx;
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
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.sirius.sdk.utils.IotaUtils.generateTag;

public class IotaChat {

    NWiseStateMachine stateMachine;
    public static int timeToLiveSec = 60;
    byte[] myVerkey;
    static List<String> protocols = Arrays.asList(BaseNWiseMessage.PROTOCOL, Ack.PROTOCOL, Ping.PROTOCOL);

    String chatName = null;

    public IotaChat(NWiseStateMachine stateMachine, byte[] myVerkey) {
        this.stateMachine = stateMachine;
        this.myVerkey = myVerkey;
    }

    private IotaChat() {

    }


    public static IotaChat createChat(String chatName, String myNickName, Context context) {
        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        GenesisTx genesisTx = new GenesisTx();
        genesisTx.setLabel(chatName);
        genesisTx.setCreatorNickname(myNickName);
        genesisTx.setCreatorDidDocParams(
                didVk.first, Base58.decode(didVk.second), context.getEndpointAddressWithEmptyRoutingKeys(), Arrays.asList(), new JSONObject());

        Client iota = IotaUtils.node();
        try {
            String tag = generateTag(Base58.decode(didVk.second));
            iota.message().
                    withIndexString(tag).
                    withData(genesisTx.toString().getBytes(StandardCharsets.UTF_8)).
                    finish();

            NWiseStateMachine stateMachine = new NWiseStateMachine();
            stateMachine.append(genesisTx);
            return new IotaChat(stateMachine, Base58.decode(didVk.second));
        } catch (Exception ex) {
            return null;
        }
    }

    public static IotaChat acceptInvitation(Invitation invitation, String nickname, Context context) {
        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        TheirEndpoint inviterEndpoint = new TheirEndpoint(invitation.getEndpoint(), invitation.getInviterVerkey(), invitation.routingKeys());
        NWiseStateMachine stateMachine = null;
        try (AbstractP2PCoProtocol cp = new CoProtocolP2PAnon(context, didVk.second, inviterEndpoint, protocols, timeToLiveSec)) {
            Request request = Request.builder().
                    setNickname(nickname).
                    setDid(didVk.first).
                    setVerkey(Base58.decode(didVk.second)).
                    setEndpoint(context.getEndpointAddressWithEmptyRoutingKeys()).
                    build();

            Pair<Boolean, com.sirius.sdk.messaging.Message> okMsg = cp.sendAndWait(request);
            if (okMsg.first && okMsg.second instanceof Response) {
                Response response = (Response) okMsg.second;
                IotaResponseAttach attach = new IotaResponseAttach(response.getAttach());
                stateMachine = processTransactions(attach.getTag()).first;
                if (response.hasPleaseAck()) {
                    Ack ack = Ack.builder().
                            setStatus(Ack.Status.OK).
                            build();
                    ack.setThreadId(response.getAckMessageId());
                    cp.send(ack);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new IotaChat(stateMachine, Base58.decode(didVk.second));
    }

    private static NWiseStateMachine processTransactions(List<NWiseTx> transactions, String genesisHashBase58) {
        NWiseStateMachine stateMachine = new NWiseStateMachine();
        Map<String, NWiseTx> hashTable = new HashMap<>();
        Map<String, List<NWiseTx>> prevHashTable = new HashMap<>();
        for (NWiseTx tx : transactions) {
            hashTable.put(Base58.encode(tx.getHash()), tx);
            if (!prevHashTable.containsKey(tx.getPreviousTxHashBase58())) {
                prevHashTable.put(tx.getPreviousTxHashBase58(), new ArrayList<>());
            }
            prevHashTable.get(tx.getPreviousTxHashBase58()).add(tx);
        }
        NWiseTx genesisTx = hashTable.get(genesisHashBase58);
        if (stateMachine.check(genesisTx)) {
            stateMachine.append(genesisTx);
            prevHashTable.remove("");
        }

        String prevHash = genesisHashBase58;

        List<NWiseTx> curTxs = prevHashTable.get(prevHash);

        return stateMachine;
    }

    private static Pair<NWiseStateMachine, String> processTransactions(String tag) {
        NWiseStateMachine stateMachine = new NWiseStateMachine();
        MessageId[] fetchedMessageIds = IotaUtils.node().getMessage().indexString(tag);
        HashMap<String, List<org.iota.client.Message>> map = new HashMap<>();
        for (MessageId msgId : fetchedMessageIds) {
            org.iota.client.Message msg = IotaUtils.node().getMessage().data(msgId);
            if (msg.payload().isPresent()) {
                JSONObject obj = new JSONObject(new String(msg.payload().get().asIndexation().data()));
                String previousMessageId = obj.optJSONObject("meta").optString("previousMessageId", "");
                if (!map.containsKey(previousMessageId))
                    map.put(previousMessageId, Arrays.asList(msg));
                else
                    map.get(previousMessageId).add(msg);
            }
        }

        if (!map.containsKey(""))
            return new Pair<>(stateMachine, "");

        String prevMessageId = "";
        org.iota.client.Message prevMessage = null;

        while (!map.isEmpty()) {
            if (map.containsKey(prevMessageId)) {
                List<org.iota.client.Message> list = map.get(prevMessageId).stream().
                        filter(m -> checkMessage(m, stateMachine)).
                        sorted(IotaUtils.msgComparator).
                        collect(Collectors.toList());
                if (list.isEmpty()) {
                    return new Pair<>(stateMachine, prevMessageId);
                } else {
                    map.remove(prevMessageId);
                    prevMessage = list.get(list.size() - 1);
                    stateMachine.append(new JSONObject(new String(prevMessage.payload().get().asIndexation().data())).optJSONObject("transaction"));
                    prevMessageId = prevMessage.id().toString();
                }
            } else {
                break;
            }
        }
        return new Pair<>(stateMachine, prevMessageId);
    }

    private static boolean checkMessage(org.iota.client.Message msg, NWiseStateMachine stateMachine) {
        return true;
    }

    public boolean acceptRequest(Request request, Context context) {
        TheirEndpoint inviteeEndpoint = new TheirEndpoint(request.getEndpoint(),
                Base58.encode(request.getVerkey()), Arrays.asList());

        try (AbstractP2PCoProtocol cp = new CoProtocolP2PAnon(context, Base58.encode(myVerkey), inviteeEndpoint, protocols, timeToLiveSec)) {
            AddParticipantTx addParticipantTx = new AddParticipantTx();
            addParticipantTx.setNickname(request.getNickname());
            addParticipantTx.setDid(request.getDid());
            addParticipantTx.setDidDoc(request.getDidDoc());
            addParticipantTx.setRole("user");


            Response response = Response.builder().
                    setLedgerType(getLedgerType()).
                    setAttach(getAttach()).
                    build();
            response.setThreadId(request.getId());

            cp.send(response);
        }

        return true;
    }

    private boolean pushTransaction(NWiseTx tx) {
        String tag = IotaUtils.generateTag(stateMachine.getGenesisCreatorVerkey());
        Pair<NWiseStateMachine, String> res = processTransactions(tag);
        JSONObject o = new JSONObject().
                put("transaction", tx).
                put("meta", new JSONObject().
                                put("previousMessageId", res.second));
        try {
            IotaUtils.node().message().
                    withIndexString(tag).
                    withData(o.toString().getBytes(StandardCharsets.UTF_8)).
                    finish();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    private JSONObject getAttach() {
        return new IotaResponseAttach(IotaUtils.generateTag(stateMachine.getGenesisCreatorVerkey()));
    }

    private String getLedgerType() {
        return "iota@v1.0";
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

    public String resolveNickname(String verkey) {
        return null;
    }

    public List<Pairwise.Their> getParticipants() {
        return null;
    }

    public boolean fetchFromLedger() {
        return false;
    }

}
