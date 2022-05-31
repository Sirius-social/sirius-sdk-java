package com.github.wenweihu86.raft.modelsExample;

public class GetResponse {
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    String value;

    @Override
    public String toString() {
        return "GetResponse{" +
                "value='" + value + '\'' +
                '}';
    }
}
