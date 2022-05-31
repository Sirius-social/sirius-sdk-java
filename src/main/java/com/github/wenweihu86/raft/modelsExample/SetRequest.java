package com.github.wenweihu86.raft.modelsExample;

public class SetRequest {
    public String getKey() {
        return key;
    }

    public SetRequest setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public SetRequest setValue(String value) {
        this.value = value;
        return this;
    }

    String key ;
    String value;
}
