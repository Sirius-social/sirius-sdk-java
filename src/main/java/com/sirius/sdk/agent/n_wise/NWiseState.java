package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.n_wise.transactions.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NWiseState {

    String label;

    class Participant {
        public String nickname;
        public String did;
        public JSONObject didDoc;
        public String role;
    }

    List<Participant> participants = new ArrayList<>();

    public NWiseState(GenesisTx genesisTx) {
        label = genesisTx.getLabel();
        Participant creator = new Participant();
        creator.nickname = genesisTx.getCreatorNickname();
        creator.did = genesisTx.getCreatorDid();
        creator.didDoc = genesisTx.getCreatorDidDoc();
        creator.role = "admin";
        participants.add(creator);
    }

    public void append(AddParticipantTx tx) {
        Participant participant = new Participant();
        participant.nickname = tx.getNickname();
        participant.didDoc = tx.getDidDoc();
        participant.role = tx.getRole();
        participants.add(participant);
    }

    public void append(UpdateMetadataTx tx) {

    }

    public void append(UpdateParticipantTx tx) {

    }

    public void append(RemoveParticipantTx tx) {

    }
}
