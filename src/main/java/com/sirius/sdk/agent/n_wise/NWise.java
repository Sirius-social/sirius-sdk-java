package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.agent.n_wise.messages.Request;
import com.sirius.sdk.agent.n_wise.transactions.NWiseTx;
import com.sirius.sdk.agent.n_wise.transactions.RemoveParticipantTx;
import com.sirius.sdk.hub.Context;
import org.apache.commons.lang.NotImplementedException;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class NWise {
    public static int timeToLiveSec = 60;
    NWiseStateMachine stateMachine;
    byte[] myVerkey;
    String internalId = null;

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
        new NWiseList(context.getNonSecrets()).addInvitationKey(internalId, key);
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

}
