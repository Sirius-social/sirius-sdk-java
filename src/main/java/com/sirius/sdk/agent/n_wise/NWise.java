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
import org.apache.commons.lang.NotImplementedException;
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

    public static NWise acceptInvitation(Invitation invitation, String nickname, Context context) {
        throw new NotImplementedException();
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
        String key = context.getCrypto().createKey();
        return Invitation.builder().
                setLabel(getChatName()).
                setInviterKey(key).
                setEndpoint(context.getEndpointAddressWithEmptyRoutingKeys()).
                setLedgerType(getLedgerType()).
                build();
    }

    public FastInvitation createFastInvitation(Context context) {
        LazySodiumJava s = LibSodium.getInstance().getLazySodium();
        try {
            KeyPair keyPair = s.cryptoSignKeypair();
            InvitationTx invitationTx = new InvitationTx();
            invitationTx.setPublicKeys(Arrays.asList(keyPair.getPublicKey().getAsBytes()));
            invitationTx.sign(context.getCrypto(), getMyDid(), myVerkey);
            pushTransaction(invitationTx);
            FastInvitation fastInvitation = FastInvitation.builder().
                    setLabel(getChatName()).
                    setLedgerType(getLedgerType()).
                    setInvitationKeyId(Base58.encode(keyPair.getPublicKey().getAsBytes())).
                    setInvitationPrivateKey(keyPair.getSecretKey().getAsBytes()).
                    setAttach(getAttach()).
                    build();
            return fastInvitation;
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

    public boolean acceptRequest(Request request, Context context) {
        TheirEndpoint inviteeEndpoint = new TheirEndpoint(request.getEndpoint(),
                Base58.encode(request.getVerkey()), Arrays.asList());

        log.info("Received request from" + Base58.encode(request.getVerkey()));

        try (AbstractP2PCoProtocol cp = new CoProtocolP2PAnon(context, Base58.encode(myVerkey), inviteeEndpoint, protocols, timeToLiveSec)) {
            AddParticipantTx addParticipantTx = new AddParticipantTx();
            addParticipantTx.setNickname(request.getNickname());
            addParticipantTx.setDid(request.getDid());
            addParticipantTx.setDidDoc(request.getDidDoc());
            addParticipantTx.setRole("user");
            addParticipantTx.sign(context.getCrypto(), getMyDid(), myVerkey);
            pushTransaction(addParticipantTx);

            log.info("Send response to" + Base58.encode(request.getVerkey()));
            Response response = Response.builder().
                    setLedgerType(getLedgerType()).
                    setAttach(getAttach()).
                    build();
            response.setThreadId(request.getId());

            cp.send(response);
        }

        return true;
    }

}
