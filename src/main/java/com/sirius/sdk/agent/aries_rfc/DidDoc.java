package com.sirius.sdk.agent.aries_rfc;

import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.diddoc.DidDocUtils;
import com.sirius.sdk.hub.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class DidDoc {
    public static final String DID = "did";

    protected JSONObject payload;

    public DidDoc(JSONObject payload) {
        this.payload = payload;
    }

    protected DidDoc() {
        payload = new JSONObject();
    }

    public String getDid() {
        return this.payload.optString("id");
    }

    public JSONObject getPayload() {
        return payload;
    }

    public JSONObject extractService(boolean highPriority, String type) {
        JSONArray services = payload.optJSONArray("service");
        if (services != null) {
            JSONObject ret = null;
            for (Object serviceObj : services) {
                JSONObject service = (JSONObject) serviceObj;
                if (!service.optString("type").equals(type))
                    continue;
                if (ret == null) {
                    ret = service;
                } else {
                    if (highPriority) {
                        if (service.optInt("priority", 0) > ret.optInt("priority", 0)) {
                            ret = service;
                        }
                    }
                }
            }
            return ret;
        }

        return null;
    }

    public JSONObject extractService() {
        return extractService(true, "IndyAgent");
    }

    public JSONObject addService(String type, Endpoint endpoint) {
        JSONObject service = new JSONObject();
        JSONArray services = payload.optJSONArray("service");
        if (services == null) {
            services = new JSONArray();
            payload.put("service", services);
        }
        service.put("id", getDid() + "#" + services.length());
        service.put("type", type);
        service.put("serviceEndpoint", endpoint.getAddress());
        if (!endpoint.getRoutingKeys().isEmpty()) {
            service.put("routingKeys", endpoint.getRoutingKeys());
        }
        services.put(service);
        return service;
    }

    public void addAgentServices(Context context) {
        List<Endpoint> endpoints = context.getEndpoints();
        for (Endpoint e : endpoints) {
            addService("DIDCommMessaging", e);
        }
    }
}
