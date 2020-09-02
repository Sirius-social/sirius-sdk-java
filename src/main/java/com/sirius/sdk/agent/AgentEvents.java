package com.sirius.sdk.agent;

import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusConnectionClosed;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.messaging.Message;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * RPC service.
 * <p>
 * Reactive nature of Smart-Contract design
 */
public class AgentEvents extends BaseAgentConnection {

    String tunnel;

    public String getBalancingGroup() {
        return balancingGroup;
    }

    String balancingGroup;

    public AgentEvents(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout) {
        super(serverAddress, credentials, p2p, timeout);
    }

    @Override
    public String path() {
        return "events";
    }

    @Override
    public void setup(Message context) {
        super.setup(context);
        // Extract load balancing info
        JSONArray balancing = context.getJSONArrayFromJSON("~balancing", new JSONArray());
        for (int i = 0; i < balancing.length(); i++) {
            JSONObject balance = balancing.getJSONObject(i);
            if ("kafka".equals(balance.getString("id"))) {
                System.out.println("balance="+balance.toString());
                JSONObject jsonObject = balance.getJSONObject("data").getJSONObject("json");
                if(!jsonObject.isNull("group_id")){
                    balancingGroup = jsonObject.getString("group_id");
                }

            }
        }


    }

    public Message pull() throws SiriusConnectionClosed, SiriusInvalidPayloadStructure {
        if (!connector.isOpen()) {
            throw new SiriusConnectionClosed("Open agent connection at first");
        }
        byte[] data = connector.read(getTimeout());
        try {
            JSONObject payload = new JSONObject(data);
            if (payload.has("protected")) {
                String message = p2p.unpack(payload.toString());
                return new Message(message);
            } else {
                return new Message(payload.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SiriusInvalidPayloadStructure(e.getMessage());
        }

    }
}
