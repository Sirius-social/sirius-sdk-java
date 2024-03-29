package com.sirius.sdk.agent.n_wise;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.n_wise.messages.*;
import com.sirius.sdk.agent.n_wise.transactions.AddParticipantTx;
import com.sirius.sdk.agent.n_wise.transactions.InvitationTx;
import com.sirius.sdk.agent.n_wise.transactions.NWiseTx;
import com.sirius.sdk.agent.n_wise.transactions.RemoveParticipantTx;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2PAnon;
import com.sirius.sdk.naclJava.LibSodium;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public abstract class NWise {
    Logger log = Logger.getLogger(NWise.class.getName());

    static List<String> protocols = Arrays.asList(BaseNWiseMessage.PROTOCOL, Ack.PROTOCOL, Ping.PROTOCOL);
    public static int timeToLiveSec = 60;
    NWiseStateMachine stateMachine;
    byte[] myVerkey;

    public static NWise restore(NWiseList.NWiseInfo info) {
        if (info.ledgerType.equals("iota@v1.0")) {
            return IotaNWise.restore(info.attach);
        }
        return null;
    }

    public abstract String getLedgerType();

    public abstract JSONObject getRestoreAttach();

    public abstract boolean fetchFromLedger();

    protected abstract boolean pushTransaction(NWiseTx tx);

    protected abstract JSONObject getAttach();

    public String getChatName() {
        return this.stateMachine.getLabel();
    }

    public Invitation createInvitation(Context context) {
        LazySodiumJava s = LibSodium.getInstance().getLazySodium();
        try {
            KeyPair keyPair = s.cryptoSignKeypair();
            InvitationTx invitationTx = new InvitationTx();
            invitationTx.setPublicKeys(Arrays.asList(keyPair.getPublicKey().getAsBytes()));
            invitationTx.sign(context.getCrypto(), getMyDid(), myVerkey);
            pushTransaction(invitationTx);
            notify(context);
            Invitation invitation = Invitation.builder().
                    setLabel(getChatName()).
                    setLedgerType(getLedgerType()).
                    setInvitationKeyId(Base58.encode(keyPair.getPublicKey().getAsBytes())).
                    setInvitationPrivateKey(keyPair.getSecretKey().getAsBytes()).
                    setAttach(getAttach()).
                    build();
            return invitation;
        } catch (SodiumException e) {
            e.printStackTrace();
        }
        return null;
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

    public boolean leave(Context context) {
        return removeParticipant(getMyDid(), context);
    }

    public boolean removeParticipant(String did, Context context) {
        RemoveParticipantTx tx = new RemoveParticipantTx();
        tx.setDid(did);
        tx.sign(context.getCrypto(), getMyDid(), myVerkey);
        boolean res = pushTransaction(tx);
        if (res)
            notify(context);
        return res;
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

    public boolean send(com.sirius.sdk.messaging.Message message, Context context) {
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

    public boolean notify(Context context) {
        return send(LedgerUpdateNotify.builder().build(), context);
    }

}
