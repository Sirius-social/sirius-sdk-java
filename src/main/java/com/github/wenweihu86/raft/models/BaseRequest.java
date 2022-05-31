package com.github.wenweihu86.raft.models;

import com.sirius.sdk.utils.GsonUtils;

public class BaseRequest {

    public transient  Server server;

    public BaseRequest setServer(Server server) {
        this.server = server;
        return this;
    }

    public Server getServer() {
        return server;
    }

    public String tosGson(){
        return GsonUtils.getDefaultGson().toJson(this);
    }
}
