package com.sirius.sdk.agent.wallet.abstract_wallet.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sirius.sdk.base.JsonSerializable;
import org.json.JSONObject;


public class CacheOptions implements JsonSerializable<CacheOptions> {

    boolean noCache;
    boolean noUpdate;
    boolean noStore;
    int minFresh = -1;

    /**
     * @param noCache  (bool, optional, false by default) Skip usage of cache,
     * @param noUpdate (bool, optional, false by default) Use only cached data, do not try to update.
     * @param noStore  (bool, optional, false by default) Skip storing fresh data if updated,
     * @param minFresh int, optional, -1 by default) Return cached data if not older than this many seconds. -1 means do not check age.
     */
    public CacheOptions(boolean noCache, boolean noUpdate, boolean noStore, int minFresh) {
        this.noCache = noCache;
        this.noUpdate = noUpdate;
        this.noStore = noStore;
        this.minFresh = minFresh;
    }

    public CacheOptions() {
    }

    @Override
    public String serialize() {
        Gson gson = new Gson();
        return gson.toJson(this, CacheOptions.class);
    }

    @Override
    public JSONObject serializeToJSONObject() {
        String string = serialize();
        return new JSONObject(string);
    }

    @Override
    public CacheOptions deserialize(String string) {
        Gson gson = new Gson();
        return gson.fromJson(string, CacheOptions.class);
    }

    @Override
    public JsonObject serializeToJsonObject() {
        return null;
    }


}
