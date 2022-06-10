package com.sirius.sdk.agent.n_wise;

import com.sirius.sdk.agent.n_wise.messages.InitialMessage;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;

public class IotaChat {

    private static IotaChat acceptInvitation() {
        return null;
    }

    public static IotaChat createChat(String name, Context context) {
        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        InitialMessage initialMessage = new InitialMessage();
        return null;
    }
}
