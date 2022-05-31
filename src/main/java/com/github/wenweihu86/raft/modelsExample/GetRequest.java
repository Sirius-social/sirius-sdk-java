package com.github.wenweihu86.raft.modelsExample;

public class GetRequest {
    public String getKey() {
        return key;
    }

    public GetRequest setKey(String key) {
        this.key = key;
        return this;
    }

    String key;

    @Override
    public String toString() {
        return "GetRequest{" +
                "key='" + key + '\'' +
                '}';
    }
}
