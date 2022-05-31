package com.github.wenweihu86.raft.models;

import com.sirius.sdk.agent.CloudAgent;

public class Server {
    public String getServerId() {
        return serverId;
    }

    public Server setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Server setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public CloudAgent getCloudAgent() {
        return cloudAgent;
    }

    public String getCloudAgentVerkey() {
        return cloudAgentVerkey;
    }

    public Server setCloudAgentVerkey(String cloudAgentVerkey) {
        this.cloudAgentVerkey = cloudAgentVerkey;
        return this;
    }

    public Server setCloudAgent(CloudAgent cloudAgent) {
        this.cloudAgent = cloudAgent;
        return this;
    }

    String serverId ;
    Endpoint endpoint = null;
    transient CloudAgent cloudAgent = null;
    String cloudAgentVerkey;

    @Override
    public String toString() {
        return "Server{" +
                "serverId=" + serverId +
                ", endpoint=" + endpoint +
                ", cloudAgent=" + cloudAgent +
                ", cloudAgentVerkey=" + cloudAgentVerkey +
                '}';
    }
}
