package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.hub.Context;
import org.bitcoinj.core.Base58;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NWiseManager {

    private Map<String, NWise> nWiseMap = null;

    Context context;
    public NWiseManager(Context context) {
        this.context = context;
    }

    public void loadFromWallet() {
        List<NWiseList.NWiseInfo> infos = new NWiseList(context.getNonSecrets()).getNWiseInfoList();
        for (NWiseList.NWiseInfo info : infos) {
            NWise nWise = NWise.restore(info);
            if (nWise != null) {
                getNWiseMap().put(info.internalId, nWise);
            }
        }
    }

    public void clearNWiseList() {
        new NWiseList(context.getNonSecrets()).clearList();
    }

    private Map<String, NWise> getNWiseMap() {
        if (nWiseMap == null) {
            nWiseMap = new ConcurrentHashMap<>();
            loadFromWallet();
        }
        return nWiseMap;
    }

    public String create(String nWiseName, String myName) {
        NWise nWise = IotaNWise.createChat(nWiseName, myName, context);
        if (nWise != null)
            return add(nWise);
        return null;
    }

    public String add(NWise nWise) {
        String internalId = new NWiseList(context.getNonSecrets()).add(nWise);
        getNWiseMap().put(internalId, nWise);
        return internalId;
    }

    public String resolveNWiseId(String senderVerkeyBase58) {
        for (Map.Entry<String, NWise> e : getNWiseMap().entrySet()) {
            if (e.getValue().getCurrentParticipantsVerkeysBase58().contains(senderVerkeyBase58))
                return e.getKey();
        }
        return null;
    }

    public NWiseParticipant resolveParticipant(String senderVerkeyBase58) {
        String internalId = resolveNWiseId(senderVerkeyBase58);
        if (internalId == null)
            return null;
        NWise nWise = getNWiseMap().getOrDefault(internalId, null);
        if (nWise != null)
            return nWise.resolveParticipant(senderVerkeyBase58);
        return null;
    }

    public List<NWiseParticipant> getParticipants(String internalId) {
        NWise nWise = getNWiseMap().get(internalId);
        if (nWise != null)
            return nWise.getParticipants();
        return Arrays.asList();
    }

    public boolean update(String internalId) {
        NWise nWise = getNWiseMap().get(internalId);
        if (nWise != null)
            return nWise.fetchFromLedger();
        return false;
    }

    public Invitation createInvitation(String internalId) {
        if (!getNWiseMap().containsKey(internalId))
            return null;
        NWise nWise = getNWiseMap().get(internalId);
        Invitation invitation = nWise.createInvitation(context);
        return invitation;
    }

    public String acceptInvitation(Invitation invitation, String nickname) {
        if (invitation.getLedgerType().equals("iota@v1.0")) {
            NWise nWise = IotaNWise.acceptInvitation(invitation, nickname, context);
            if (nWise != null) {
                return add(nWise);
            }
        }
        return null;
    }

    public boolean send(String internalId, Message msg) {
        if (!getNWiseMap().containsKey(internalId))
            return false;
        return getNWiseMap().get(internalId).send(msg, context);
    }

    public boolean leave(String internalId, Context context) {
        if (!getNWiseMap().containsKey(internalId))
            return false;
        boolean res = getNWiseMap().get(internalId).leave(context);
        if (res) {
            new NWiseList(context.getNonSecrets()).remove(internalId);
            getNWiseMap().remove(internalId);
        }
        return res;
    }

    public boolean getNotify(String senderVerkeyBase58) {
        String internalId = resolveNWiseId(senderVerkeyBase58);
        if (internalId != null)
            return update(internalId);
        return false;
    }

    public NWiseParticipant getMe(String internalId, Context context) {
        if (!getNWiseMap().containsKey(internalId))
            return null;
        NWise nWise = getNWiseMap().get(internalId);
        return nWise.resolveParticipant(Base58.encode(nWise.myVerkey));
    }
}
