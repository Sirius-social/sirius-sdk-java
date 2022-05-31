package com.github.wenweihu86.raft.models;

import java.util.List;

public class AddPeersRequest  extends BaseRequest{
    public List<Server> getServersList() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    List<Server> servers ;

    public int getServersCount() {
        return servers.size();
    }
}
