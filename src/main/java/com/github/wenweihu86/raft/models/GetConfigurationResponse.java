package com.github.wenweihu86.raft.models;

import java.util.ArrayList;
import java.util.List;

public class GetConfigurationResponse extends BaseResponse{
    ResCode resCode ;
    String resMsg;
    Server leader;
    List<Server> servers = new ArrayList<>();

    public ResCode getResCode() {
        return resCode;
    }

    public void setResCode(ResCode resCode) {
        this.resCode = resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    public Server getLeader() {
        return leader;
    }

    public void setLeader(Server leader) {
        this.leader = leader;
    }

    public List<Server> getServersList() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }



    public GetConfigurationResponse addAllServers(List<Server> serversList) {
        servers.addAll(serversList);
        return this;
    }

    @Override
    public String toString() {
        return "GetConfigurationResponse{" +
                "resCode=" + resCode +
                ", resMsg='" + resMsg + '\'' +
                ", leader=" + leader +
                ", servers=" + servers +
                '}';
    }
}
