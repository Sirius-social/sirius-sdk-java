package com.github.wenweihu86.raft.models;

public class Endpoint {
    String host;

    public String getHost() {
        return host;
    }

    public Endpoint setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public Endpoint setPort(Integer port) {
        this.port = port;
        return this;
    }

    Integer port ;

    @Override
    public String toString() {
        return "Endpoint{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
