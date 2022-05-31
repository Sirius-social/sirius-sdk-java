package com.github.wenweihu86.raft.models;

public class InstallSnapshotResponse extends BaseResponse{
    public ResCode getResCode() {
        return resCode;
    }

    public void setResCode(ResCode resCode) {
        this.resCode = resCode;
    }

    public Long getTerm() {
        return term;
    }

    public void setTerm(Long term) {
        this.term = term;
    }

    ResCode resCode;
    Long term ;
}
