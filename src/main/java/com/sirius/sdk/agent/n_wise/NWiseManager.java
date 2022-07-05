package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.agent.n_wise.messages.Request;
import com.sirius.sdk.hub.Context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NWiseManager {

    private static Map<String, NWise> nWiseMap = null;

    public static void loadFromWallet(Context context) {
        List<NWiseList.NWiseInfo> infos = new NWiseList(context.getNonSecrets()).getNWiseInfoList();
        for (NWiseList.NWiseInfo info : infos) {
            NWise nWise = NWise.restore(info);
            if (nWise != null) {
                getNWiseMap(context).put(info.internalId, nWise);
            }
        }
    }

    private static Map<String, NWise> getNWiseMap(Context context) {
        if (nWiseMap == null) {
            nWiseMap = new ConcurrentHashMap<>();
            loadFromWallet(context);
        }
        return nWiseMap;
    }

    public static NWise create(String nWiseName, String myName, Context context) {
        NWise nWise = IotaNWise.createChat(nWiseName, myName, context);
        if (nWise != null)
            add(nWise, context);
        return nWise;
    }

    public static void add(NWise nWise, Context context) {
        String internalId = new NWiseList(context.getNonSecrets()).add(nWise);
        getNWiseMap(context).put(internalId, nWise);
    }

    public static String resolveNWiseId(String senderVerkeyBase58, Context context) {
        for (Map.Entry<String, NWise> e : getNWiseMap(context).entrySet()) {
            if (e.getValue().getCurrentParticipantsVerkeysBase58().contains(senderVerkeyBase58))
                return e.getKey();
        }
        return null;
    }

    public static Invitation createUniqueInvitation(String internalId, Context context) {
        if (!getNWiseMap(context).containsKey(internalId))
            return null;
        NWise nWise = getNWiseMap(context).get(internalId);
        Invitation invitation = nWise.createInvitation(context);
        new NWiseList(context.getNonSecrets()).addInvitationKey(internalId, invitation.getInviterVerkey());
        return invitation;
    }

    public static NWise acceptInvitation(Invitation invitation, String nickname, Context context) {
        if (invitation.getLedgerType().equals("iota@1.0")) {
            NWise nWise = IotaNWise.acceptInvitation(invitation, nickname, context);
            if (nWise != null) {
                add(nWise, context);
                return nWise;
            }
        }
        return null;
    }

    public static boolean acceptRequest(Request request, String invitationKeyBase58, Context context) {
        if (!new NWiseList(context.getNonSecrets()).hasInvitationKey(invitationKeyBase58)) {
            return false;
        }

        String internalId = new NWiseList(context.getNonSecrets()).getNWiseInfoByInvitation(invitationKeyBase58).internalId;
        NWise nWise = getNWiseMap(context).get(internalId);
        if (nWise instanceof IotaNWise) {
            if (((IotaNWise) nWise).acceptRequest(request, invitationKeyBase58, context)) {
                new NWiseList(context.getNonSecrets()).removeInvitationKey(invitationKeyBase58);
                return true;
            }
        }

        return false;
    }
}
