package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProtocolMessage;
import com.sirius.sdk.agent.n_wise.messages.*;
import com.sirius.sdk.agent.n_wise.transactions.AddParticipantTx;
import com.sirius.sdk.agent.n_wise.transactions.GenesisTx;
import com.sirius.sdk.agent.n_wise.transactions.NWiseTx;
import com.sirius.sdk.hub.Context;
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

public class IotaNWise extends NWise {

    public IotaNWise(NWiseStateMachine stateMachine, byte[] myVerkey) {
        this.stateMachine = stateMachine;
        this.myVerkey = myVerkey;
    }

    public static IotaNWise createChat(String chatName, String myNickName, Context context) {
        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        GenesisTx genesisTx = new GenesisTx();
        genesisTx.setLabel(chatName);
        genesisTx.setCreatorNickname(myNickName);
        genesisTx.setCreatorDidDocParams(
                didVk.first, Base58.decode(didVk.second), context.getEndpointAddressWithEmptyRoutingKeys(), Arrays.asList(), new JSONObject());
        genesisTx.sign(context.getCrypto(), didVk.first, Base58.decode(didVk.second));

        Client iota = IotaUtils.node();
        try {
            String tag = generateTag(Base58.decode(didVk.second));
            JSONObject o = new JSONObject().
                    put("transaction", genesisTx).
                    put("meta", new JSONObject());
            iota.message().
                    withIndexString(tag).
                    withData(o.toString().getBytes(StandardCharsets.UTF_8)).
                    finish();

            NWiseStateMachine stateMachine = new NWiseStateMachine();
            stateMachine.append(genesisTx);
            return new IotaNWise(stateMachine, Base58.decode(didVk.second));
        } catch (Exception ex) {
            return null;
        }
    }

    public static IotaNWise acceptInvitation(Invitation invitation, String nickname, Context context) {
        IotaResponseAttach attach = new IotaResponseAttach(invitation.getAttach());
        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        AddParticipantTx tx = new AddParticipantTx();
        tx.setDid(didVk.first);
        JSONObject didDoc = ConnProtocolMessage.buildDidDoc(didVk.first, didVk.second, context.getEndpointAddressWithEmptyRoutingKeys());
        tx.setDidDoc(didDoc);
        tx.setNickname(nickname);
        tx.sign(invitation.getInvitationKeyId(), invitation.getInvitationPrivateKey());
        Pair<Boolean, NWiseStateMachine> res = pushTransactionToIota(tx, attach.getTag());
        if (res.first) {
            IotaNWise nWise = new IotaNWise(res.second, Base58.decode(didVk.second));
            nWise.notify(context);
            return nWise;
        } else {
            return null;
        }
    }

    public JSONObject getRestoreAttach() {
        return new JSONObject().
                put("tag", generateTag(stateMachine.getGenesisCreatorVerkey())).
                put("myVerkeyBase58", Base58.encode(myVerkey));
    }

    public static IotaNWise restore(JSONObject attach) {
        String tag = attach.optString("tag");
        String myVerkeyBase58 = attach.optString("myVerkeyBase58");
        NWiseStateMachine stateMachine = processTransactions(tag).first;
        return new IotaNWise(stateMachine, Base58.decode(myVerkeyBase58));
    }

    private static Pair<NWiseStateMachine, String> processTransactions(String tag) {
        NWiseStateMachine stateMachine = new NWiseStateMachine();
        String prevMessageId = "";
        try {
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
        } catch (Exception e) {
            return new Pair<>(stateMachine, prevMessageId);
        }
    }

    @Override
    public boolean fetchFromLedger() {
        stateMachine = processTransactions(generateTag(stateMachine.getGenesisCreatorVerkey())).first;
        return true;
    }

    private static boolean checkMessage(org.iota.client.Message msg, NWiseStateMachine stateMachine) {
        return true;
    }

    @Override
    protected boolean pushTransaction(NWiseTx tx) {
        String tag = IotaUtils.generateTag(stateMachine.getGenesisCreatorVerkey());
        Pair<Boolean, NWiseStateMachine> res = pushTransactionToIota(tx, tag);
        stateMachine = res.second;
        return res.first;
    }

    private static Pair<Boolean, NWiseStateMachine> pushTransactionToIota(NWiseTx tx, String tag) {
        Pair<NWiseStateMachine, String> res = processTransactions(tag);
        JSONObject o = new JSONObject().
                put("transaction", tx).
                put("meta", new JSONObject().
                        put("previousMessageId", res.second));
        try {
            if (!res.first.check(tx)) {
                return new Pair<>(false, res.first);
            }
            IotaUtils.node().message().
                    withIndexString(tag).
                    withData(o.toString().getBytes(StandardCharsets.UTF_8)).
                    finish();
            res.first.append(tx);
        } catch (Exception ex) {
            return new Pair<>(false, res.first);
        }
        return new Pair<>(true, res.first);
    }

    @Override
    protected JSONObject getAttach() {
        return new IotaResponseAttach(IotaUtils.generateTag(stateMachine.getGenesisCreatorVerkey()));
    }

    @Override
    public String getLedgerType() {
        return "iota@v1.0";
    }
}
