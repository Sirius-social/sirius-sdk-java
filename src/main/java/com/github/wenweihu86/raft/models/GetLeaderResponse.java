package com.github.wenweihu86.raft.models;

public class GetLeaderResponse extends  BaseResponse {
    ResCode resCode ;
    String resMsg ;

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

    public Endpoint getLeader() {
        return leader;
    }

    public void setLeader(Endpoint leader) {
        this.leader = leader;
    }

    Endpoint leader ;

    @Override
    public String toString() {
        return "GetLeaderResponse{" +
                "resCode=" + resCode +
                ", resMsg='" + resMsg + '\'' +
                ", leader=" + leader +
                '}';
    }
}
