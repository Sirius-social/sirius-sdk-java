package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.n_wise.transactions.*;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.suites.JcsEd25519Signature2020SignatureSuite;
import info.weboftrust.ldsignatures.verifier.JcsEd25519Signature2020LdVerifier;
import info.weboftrust.ldsignatures.verifier.LdVerifier;
import io.ipfs.multibase.Multibase;
import org.apache.commons.lang.NotImplementedException;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NWiseStateMachine {

    String label;
    byte[] genesisCreatorVerkey;

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
        }
        return true;
    }

    public boolean check(GenesisTx tx) {
        LdVerifier<JcsEd25519Signature2020SignatureSuite> ldVerifier = new JcsEd25519Signature2020LdVerifier(tx.getCreatorVerkey());
        JsonLDObject ldObject = JsonLDObject.fromJson(tx.toString());
        try {
            return ldVerifier.verify(ldObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
