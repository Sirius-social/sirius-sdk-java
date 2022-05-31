package com.github.wenweihu86.raft.models;

import java.util.List;

public class RemovePeersRequest extends BaseRequest {
    public List<Server> getServersList() {
        return servers;
    }

    public void setServersList(List<Server> servers) {
        this.servers = servers;
    }

    List<Server> servers ;

    public int getServersCount() {
        return servers.size();
    }

    @Override
    public String toString() {
        return "RemovePeersRequest{" +
                "servers=" + servers +
                '}';
    }
}
