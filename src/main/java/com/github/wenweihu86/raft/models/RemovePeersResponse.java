package com.github.wenweihu86.raft.models;

public class RemovePeersResponse extends BaseResponse {
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

    ResCode resCode;
    String resMsg;
}
