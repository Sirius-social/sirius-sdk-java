package com.github.wenweihu86.raft.models;

import com.sirius.sdk.agent.RemoteParams;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    public Configuration(Configuration configuration) {
        this.servers = configuration.servers;
    }
    public Configuration() {

    }

    public List<Server> getServersList() {
        return servers;
    }

    public void setServersList(List<Server> servers) {
        this.servers = servers;
    }

    List<Server> servers = new ArrayList<>();


    public int getServersCount() {
        return servers.size();
    }

    public void addServers(Server server) {
        servers.add(server);
    }

    public Configuration addAllServers(List<Server> serversList) {
        servers.addAll(serversList);
        return this;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "servers=" + servers +
                '}';
    }
}
