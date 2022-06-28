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

    class Participant {
        public String nickname;
        public String did;
        public JSONObject didDoc;
        public String role;
    }

    List<Participant> participants = new ArrayList<>();

    public NWiseStateMachine() {

    }

    public boolean check(JSONObject tx) {
        return true;
    }

    public boolean append(GenesisTx genesisTx) {
        label = genesisTx.getLabel();
        Participant creator = new Participant();
        creator.nickname = genesisTx.getCreatorNickname();
        creator.did = genesisTx.getCreatorDid();
        creator.didDoc = genesisTx.getCreatorDidDoc();
        creator.role = "admin";
        participants.add(creator);
        this.genesisCreatorVerkey = Base58.decode(creator.didDoc.optJSONArray("publicKey").getJSONObject(0).getString("publicKeyBase58"));
        return true;
    }

    public boolean append(AddParticipantTx tx) {
        Participant participant = new Participant();
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
        return true;
    }

    public byte[] getGenesisCreatorVerkey() {
        return genesisCreatorVerkey;
    }

}
