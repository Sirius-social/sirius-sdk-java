package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.agent.n_wise.messages.Request;
import com.sirius.sdk.agent.n_wise.transactions.NWiseTx;
import com.sirius.sdk.agent.n_wise.transactions.RemoveParticipantTx;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2PAnon;
import org.apache.commons.lang.NotImplementedException;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class NWise {
    public static int timeToLiveSec = 60;
    NWiseStateMachine stateMachine;
    byte[] myVerkey;

    public static NWise restore(NWiseList.NWiseInfo info) {
        if (info.ledgerType.equals("iota@v1.0")) {
            return IotaNWise.restore(info.attach);
        }
        return null;
    }

    public static NWise acceptInvitation(Invitation invitation, String nickname, Context context) {
        throw new NotImplementedException();
    }

    public abstract String getLedgerType();

    public abstract JSONObject getRestoreAttach();

    public abstract boolean fetchFromLedger();

    protected abstract boolean pushTransaction(NWiseTx tx);

    public String getChatName() {
        return this.stateMachine.getLabel();
    }

    public Invitation createInvitation(Context context) {
        String key = context.getCrypto().createKey();
        return Invitation.builder().
                setLabel(getChatName()).
                setInviterKey(key).
                setEndpoint(context.getEndpointAddressWithEmptyRoutingKeys()).
                setLedgerType(getLedgerType()).
                build();
    }

    public List<NWiseParticipant> getParticipants() {
        fetchFromLedger();
        return stateMachine.participants;
    }

    public List<String> getCurrentParticipantsVerkeysBase58() {
        List<String> res = new ArrayList<>();
        for (NWiseParticipant p : getParticipants()) {
            res.add(Base58.encode(p.getVerkey()));
        }
        return res;
    }

    public boolean leave() {
        return removeParticipant(getMyDid());
    }

    public boolean removeParticipant(String did) {
        RemoveParticipantTx tx = new RemoveParticipantTx();
        tx.setDid(did);
        return pushTransaction(tx);
    }

    public String getMyDid() {
        return stateMachine.resolveDid(this.myVerkey);
    }

    public String resolveNickname(String verkeyBase58) {
        return stateMachine.resolveNickname(Base58.decode(verkeyBase58));
    }

    public NWiseParticipant resolveParticipant(String verkeyBase58) {
        return stateMachine.resolveParticipant(Base58.decode(verkeyBase58));
    }

    public boolean send(Message message, Context context) {
        List<NWiseParticipant> participants = getParticipants();
        for (NWiseParticipant participant : participants) {
            if (Arrays.equals(participant.getVerkey(), this.myVerkey))
                break;
            TheirEndpoint theirEndpoint = new TheirEndpoint(participant.getEndpoint(), Base58.encode(participant.getVerkey()), Arrays.asList());
            try (AbstractP2PCoProtocol cp = new CoProtocolP2PAnon(context, Base58.encode(myVerkey), theirEndpoint, Arrays.asList(Message.PROTOCOL), timeToLiveSec)) {
                cp.send(message);
            }
        }
        return true;
    }

}
