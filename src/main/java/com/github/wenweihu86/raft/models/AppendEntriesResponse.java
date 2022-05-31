package com.github.wenweihu86.raft.models;

public class AppendEntriesResponse extends BaseResponse {
     ResCode resCode ; // 跟随者包含了匹配上 prevLogIndex 和 prevLogTerm 的日志时为真
     Long term; // 当前的任期号，用于领导人去更新自己

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

    public Long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(Long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    Long lastLogIndex ;
}
