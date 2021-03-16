package com.sirius.sdk.agent.microledgers;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Utils {

    public static byte[] serializeOrdering(JSONObject value) {
        return value.toString().getBytes(StandardCharsets.UTF_8);
    }
}
