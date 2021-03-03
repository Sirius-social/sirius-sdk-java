package com.sirius.sdk.agent;

import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;

public class Event extends Message {

    Pairwise pairwise;
    public Event(Pairwise pairwise,String message) {
        super(message);
        this.pairwise = pairwise;
    }

    public Message message() {
        if (getMessageObj().has("message")) {
            JSONObject msgJson = getMessageObj().getJSONObject("message");
            Message restored = null;
            try {
                restored = restoreMessageInstance(msgJson.toString()).second;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                return null;
            }
            return restored;
        }
        return null;
    }

    public String getRecipientVerkey() {
        return getMessageObj().optString("recipient_verkey");
    }
}
