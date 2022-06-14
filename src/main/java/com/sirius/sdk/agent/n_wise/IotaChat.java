package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.n_wise.messages.InitialMessage;
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
}
