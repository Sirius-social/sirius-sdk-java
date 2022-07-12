package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.n_wise.transactions.*;
import com.sirius.sdk.utils.Pair;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.suites.JcsEd25519Signature2020SignatureSuite;
import info.weboftrust.ldsignatures.verifier.JcsEd25519Signature2020LdVerifier;
import info.weboftrust.ldsignatures.verifier.LdVerifier;
import io.ipfs.multibase.Multibase;
import org.apache.commons.lang.NotImplementedException;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.util.*;

public class NWiseStateMachine {

    String label;
    byte[] genesisCreatorVerkey;
    Map<String, byte[]> invitationKeys = new HashMap<>();

    List<NWiseParticipant> participants = new ArrayList<>();

    public List<NWiseParticipant> getParticipants() {
        return participants;
    }

    public NWiseStateMachine() {

    }

    public String getLabel() {
        return label;
    }

    public boolean check(JSONObject jsonObject) {
        String type = jsonObject.optString("type");
        if (type.equals("genesisTx")) {
            return check(new GenesisTx(jsonObject));
        } else if (type.equals("addParticipantTx")) {
            return check(new AddParticipantTx(jsonObject));
        } else if (type.equals("invitationTx")) {
            return check(new InvitationTx(jsonObject));
        }
        return true;
    }

    public boolean check(GenesisTx tx) {
        return check(tx, tx.getCreatorVerkey());
    }

    private boolean check(JSONObject o, byte[] verkey) {
        LdVerifier<JcsEd25519Signature2020SignatureSuite> ldVerifier = new JcsEd25519Signature2020LdVerifier(verkey);
        JsonLDObject ldObject = JsonLDObject.fromJson(o.toString());
        try {
            return ldVerifier.verify(ldObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean check(AddParticipantTx tx) {
        if (!tx.has("proof"))
            return false;
        JSONObject proof = tx.getJSONObject("proof");
        String verificationMethod = proof.optString("verificationMethod");
        if (invitationKeys.containsKey(verificationMethod)) {
            byte[] verkey = invitationKeys.get(verificationMethod);
            if (tx.getRole().equals("user"))
                return check(tx, verkey);
            else
                return false;
        }
        byte[] verkey = getVerificationMethodPublicKey(verificationMethod);
        if (verkey == null)
            return false;
        if (!resolveParticipant(verkey).role.equals("admin"))
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
        if (!resolveParticipant(verkey).role.equals("admin"))
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
        label = genesisTx.getLabel();
        NWiseParticipant creator = new NWiseParticipant();
        creator.nickname = genesisTx.getCreatorNickname();
        creator.did = genesisTx.getCreatorDid();
        creator.didDoc = genesisTx.getCreatorDidDoc();
        creator.role = "admin";
        participants.add(creator);
        this.genesisCreatorVerkey = Base58.decode(creator.didDoc.optJSONArray("publicKey").getJSONObject(0).getString("publicKeyBase58"));
        return true;
    }

    public boolean append(AddParticipantTx tx) {
        NWiseParticipant participant = new NWiseParticipant();
        participant.nickname = tx.getNickname();
        participant.did = tx.getDid();
        participant.didDoc = tx.getDidDoc();
        participant.role = tx.getRole();
        participants.add(participant);
        return true;
    }

    public boolean append(InvitationTx tx) {
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
        participants.removeIf((NWiseParticipant p) -> {
            return p.did.equals(tx.getDid());
        });
        return true;
    }

    public boolean append(JSONObject jsonObject) {
        String type = jsonObject.optString("type");
        if (type.equals("genesisTx")) {
            return append(new GenesisTx(jsonObject));
        }
        if (type.equals("addParticipantTx")) {
            return append(new AddParticipantTx(jsonObject));
        }
        if (type.equals("removeParticipantTx")) {
            return append(new RemoveParticipantTx(jsonObject));
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
