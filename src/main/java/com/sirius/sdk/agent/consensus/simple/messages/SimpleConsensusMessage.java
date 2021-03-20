package com.sirius.sdk.agent.consensus.simple.messages;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SimpleConsensusMessage extends AriesProtocolMessage {
    public static final String PROTOCOL = "simple-consensus";

    public SimpleConsensusMessage(String msg) {
        super(msg);
    }

    public List<String> getParticipants() {
        List<String> res = new ArrayList<>();
        if (getMessageObj().has("participants")) {
            JSONArray jArr = getMessageObj().optJSONArray("participants");
            for (Object o : jArr) {
                if (o instanceof String)
                    res.add((String) o);
            }
        }

        return res;
    }

    public static abstract class Builder<B extends Builder<B>> extends AriesProtocolMessage.Builder<B> {
        List<String> participants = null;

        public B setParticipants(List<String> participants) {
            this.participants = participants;
            return self();
        }

        protected Builder() {}

        @Override
        protected JSONObject generateJSON() {
            JSONObject jsonObject = super.generateJSON();

            if (participants != null) {
                jsonObject.put("participants", participants);
            } else {
                jsonObject.put("participants", new JSONArray());
            }

            return jsonObject;
        }
    }
}
