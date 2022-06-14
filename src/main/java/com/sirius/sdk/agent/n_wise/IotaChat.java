package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.n_wise.messages.InitialMessage;
import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.agent.n_wise.messages.Request;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;

public class IotaChat {

    private static IotaChat acceptInvitation() {
        return null;
    }

    private IotaChat() {

    }

    public static IotaChat createChat(String chatName, String myNickName, Context context) {
        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        InitialMessage initialMessage = new InitialMessage();
        initialMessage.chatName = chatName;
        initialMessage.creatorDid = didVk.first;
        initialMessage.creatorEndpoint = context.getEndpointWithEmptyRoutingKeys();
        initialMessage.creatorNickname = myNickName;

        return new IotaChat();
    }

    public static IotaChat accept(Invitation invitation) {

        return new IotaChat();
    }

    public boolean accept(Request request) {

        return false;
    }

    public Invitation createInvitation(Context context) {
        String connectionKey = context.getCrypto().createKey();
        Invitation invitation = new Invitation();

        return invitation;
    }

    public boolean send(Message hello_world) {

        return false;
    }
}
