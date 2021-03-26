package com.sirius.sdk.agent.connections;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class RoutingBatch extends JSONObject {

    public RoutingBatch(List<String> theirVk, String endpoint, String myVk, List<String> routingKeys) {
        super();
        put("recipient_verkeys", new JSONArray(theirVk));
        put("endpoint_address", endpoint);
        put("sender_verkey", myVk);
        put("routing_keys", new JSONArray(routingKeys));
    }
}
