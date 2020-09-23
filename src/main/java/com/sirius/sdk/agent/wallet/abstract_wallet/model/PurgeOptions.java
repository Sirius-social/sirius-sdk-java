package com.sirius.sdk.agent.wallet.abstract_wallet.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sirius.sdk.base.JsonSerializable;
import org.json.JSONObject;

public class PurgeOptions implements JsonSerializable<PurgeOptions> {
    int maxAge = -1;

    public PurgeOptions(int maxAge) {
        this.maxAge = maxAge;
    }

    public PurgeOptions() {
    }

    @Override
    public String serialize() {
        Gson gson = new Gson();
        return  gson.toJson(this,PurgeOptions.class);
    }

    @Override
    public JSONObject serializeToJSONObject() {
        String string = serialize();
        return new JSONObject(string);
    }

    @Override
    public PurgeOptions deserialize(String string) {
        Gson gson = new Gson();
        return  gson.fromJson(string,PurgeOptions.class);
    }

    @Override
    public JsonObject serializeToJsonObject() {
        return null;
    }
}

