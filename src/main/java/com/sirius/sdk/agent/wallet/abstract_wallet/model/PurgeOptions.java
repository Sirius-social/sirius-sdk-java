package com.sirius.sdk.agent.wallet.abstract_wallet.model;

import com.google.gson.Gson;
import com.sirius.sdk.base.JsonSerializable;

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
    public PurgeOptions deserialize(String string) {
        Gson gson = new Gson();
        return  gson.fromJson(string,PurgeOptions.class);
    }
}

