package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.n_wise.transactions.*;
import org.apache.commons.lang.NotImplementedException;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public boolean check(JSONObject tx) {
        return true;
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
        throw new NotImplementedException();
    }

    public boolean append(JSONObject jsonObject) {
        String type = jsonObject.optString("type");
        if (type.equals("genesisTx")) {
            return append(new GenesisTx(jsonObject));
        }
        if (type.equals("addParticipantTx")) {
            return append(new AddParticipantTx(jsonObject));
        }
        return false;
    }

    public byte[] getGenesisCreatorVerkey() {
        return genesisCreatorVerkey;
    }

}
