package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.n_wise.transactions.*;
import com.sirius.sdk.utils.JcsEd25519Signature2020LdVerifier;
import com.sirius.sdk.utils.Pair;
import org.apache.commons.lang.NotImplementedException;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.util.*;

public class NWiseStateMachine {

    boolean created = false;
    String label;
    byte[] genesisCreatorVerkey;
    Map<String, byte[]> invitationKeys = new HashMap<>();

    List<NWiseParticipant> participants = new ArrayList<>();
    NWiseParticipant currentOwner;

    public List<NWiseParticipant> getParticipants() {
        return participants;
    }

    public NWiseStateMachine() {

    }

    public String getLabel() {
        return label;
    }

    public NWiseParticipant getCurrentOwner() {
        return currentOwner;
    }

    public boolean check(JSONObject jsonObject) {
        String type = jsonObject.optString("type");
        switch (type) {
            case "genesisTx":
                return check(new GenesisTx(jsonObject));
            case "addParticipantTx":
                return check(new AddParticipantTx(jsonObject));
            case "invitationTx":
                return check(new InvitationTx(jsonObject));
            case "removeParticipantTx":
                return check(new RemoveParticipantTx(jsonObject));
            case "newOwnerTx":
                return check(new NewOwnerTx(jsonObject));
        }
        return true;
    }

    public boolean check(GenesisTx tx) {
        if (created)
            return false;
        return check(tx, tx.getCreatorVerkey());
    }

    private boolean check(JSONObject o, byte[] verkey) {
        JcsEd25519Signature2020LdVerifier verifier = new JcsEd25519Signature2020LdVerifier(verkey);
        return verifier.verify(o);
    }

    public boolean check(AddParticipantTx tx) {
        if (!tx.has("proof"))
            return false;
        JSONObject proof = tx.getJSONObject("proof");
        String verificationMethod = proof.optString("verificationMethod");
        if (invitationKeys.containsKey(verificationMethod)) {
            byte[] verkey = invitationKeys.get(verificationMethod);
            return check(tx, verkey);
        }
        byte[] verkey = getVerificationMethodPublicKey(verificationMethod);
        if (verkey == null)
            return false;
        return check(tx, verkey);
    }

    public boolean check(InvitationTx tx) {
        if (!tx.has("proof"))
            return false;
        JSONObject proof = tx.getJSONObject("proof");
        String verificationMethod = proof.optString("verificationMethod");
        byte[] verkey = getVerificationMethodPublicKey(verificationMethod);
        if (verkey == null)
            return false;
        return check(tx, verkey);
    }

    public boolean check(RemoveParticipantTx tx) {
        if (!tx.has("proof"))
            return false;
        JSONObject proof = tx.getJSONObject("proof");
        String verificationMethod = proof.optString("verificationMethod");
        byte[] verkey = getVerificationMethodPublicKey(verificationMethod);
        if (verkey == null)
            return false;
        NWiseParticipant signer = resolveParticipant(verkey);
        if (signer.did.equals(tx.getDid()) || signer.did.equals(currentOwner.did))
            return check(tx, verkey);
        return false;
    }

    public boolean check(NewOwnerTx tx) {
        if (!tx.has("proof"))
            return false;
        JSONObject proof = tx.getJSONObject("proof");
        String verificationMethod = proof.optString("verificationMethod");
        byte[] verkey = getVerificationMethodPublicKey(verificationMethod);
        if (verkey == null)
            return false;
        NWiseParticipant signer = resolveParticipant(verkey);
        if (!signer.did.equals(currentOwner.did))
            return false;
        return check(tx, verkey);
    }

    private byte[] getVerificationMethodPublicKey(String verificationMethodUri) {
        for (NWiseParticipant p : participants) {
            if (verificationMethodUri.startsWith(p.did)) {
                return p.getVerkey();
            }
        }
        return null;
    }

    public boolean append(GenesisTx genesisTx) {
        if (!check(genesisTx))
            return false;
        created = true;
        label = genesisTx.getLabel();
        NWiseParticipant creator = new NWiseParticipant();
        creator.nickname = genesisTx.getCreatorNickname();
        creator.did = genesisTx.getCreatorDid();
        creator.didDoc = genesisTx.getCreatorDidDoc();
        participants.add(creator);
        currentOwner = creator;
        this.genesisCreatorVerkey = Base58.decode(creator.didDoc.optJSONArray("publicKey").getJSONObject(0).getString("publicKeyBase58"));
        return true;
    }

    public boolean append(AddParticipantTx tx) {
        if (!check(tx))
            return false;
        NWiseParticipant participant = new NWiseParticipant();
        participant.nickname = tx.getNickname();
        participant.did = tx.getDid();
        participant.didDoc = tx.getDidDoc();
        participants.add(participant);

        JSONObject proof = tx.getJSONObject("proof");
        String verificationMethod = proof.optString("verificationMethod");
        if (invitationKeys.containsKey(verificationMethod))
            invitationKeys.remove(verificationMethod);

        return true;
    }

    public boolean append(InvitationTx tx) {
        if (!check(tx))
            return false;
        List<Pair<String, byte[]>> keys = tx.getPublicKeys();
        for (Pair<String, byte[]> p : keys) {
            invitationKeys.put(p.first, p.second);
        }
        return true;
    }

    public boolean append(UpdateMetadataTx tx) {
        throw new NotImplementedException();
    }

    public boolean append(UpdateParticipantTx tx) {
        throw new NotImplementedException();
    }

    public boolean append(RemoveParticipantTx tx) {
        if (!check(tx))
            return false;
        participants.removeIf((NWiseParticipant p) -> {
            return p.did.equals(tx.getDid());
        });
        return true;
    }

    public boolean append(NewOwnerTx tx) {
        if (!check(tx))
            return false;
        for (NWiseParticipant p : participants) {
            if (p.did.equals(tx.getDid())) {
                currentOwner = p;
                return true;
            }
        }
        return false;
    }

    public boolean append(JSONObject jsonObject) {
        String type = jsonObject.optString("type");
        switch (type) {
            case "genesisTx":
                return append(new GenesisTx(jsonObject));
            case "addParticipantTx":
                return append(new AddParticipantTx(jsonObject));
            case "removeParticipantTx":
                return append(new RemoveParticipantTx(jsonObject));
            case "invitationTx":
                return append(new InvitationTx(jsonObject));
            case "newOwnerTx":
                return append(new NewOwnerTx(jsonObject));
        }
        return false;
    }

    public byte[] getGenesisCreatorVerkey() {
        return genesisCreatorVerkey;
    }

    public String resolveNickname(byte[] verkey) {
        for (NWiseParticipant p : participants) {
            if (Arrays.equals(p.getVerkey(), verkey))
                return p.nickname;
        }
        return null;
    }

    public NWiseParticipant resolveParticipant(byte[] verkey) {
        for (NWiseParticipant p : participants) {
            if (Arrays.equals(p.getVerkey(), verkey))
                return p;
        }
        return null;
    }

    public String resolveDid(byte[] verkey) {
        for (NWiseParticipant p : participants) {
            if (Arrays.equals(p.getVerkey(), verkey))
                return p.did;
        }
        return null;
    }

}
