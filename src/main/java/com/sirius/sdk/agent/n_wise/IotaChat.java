package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.n_wise.messages.InitialMessage;
import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.agent.n_wise.messages.Request;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;

import java.util.List;

public class IotaChat {

    String chatName = null;

    private static IotaChat acceptInvitation() {
        return null;
    }

    private IotaChat() {

    }

    public static IotaChat createChat(String chatName, String myNickName, Context context) {
        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        InitialMessage initialMessage = InitialMessage.builder().
                setLabel(chatName).
                setCreatorNickName(myNickName).
                setCreatorDid(didVk.first).
                setCreatorVerkey(didVk.second).setCreatorEndpoint(context.getEndpointAddressWithEmptyRoutingKeys())
                .build();

        return new IotaChat();
    }

    public static IotaChat accept(Invitation invitation, String nickName, Context context) {

        return new IotaChat();
    }

    public boolean accept(Request request, Context context) {

        return false;
    }

    public Invitation createInvitation(Context context) {
        return Invitation.builder().
                setLabel(chatName).
                setInviterKey(context.getCrypto().createKey()).
                setEndpoint(context.getEndpointAddressWithEmptyRoutingKeys()).
                build();
    }

    public boolean send(Message message) {

        return false;
    }

    public String myKey() {
        return null;
    }

    public String resolveNickName(String verkey) {
        return null;
    }

    public List<Pairwise.Their> getParticipants() {
        return null;
    }

    public boolean fetchFromLedger() {
        return false;
    }


}
