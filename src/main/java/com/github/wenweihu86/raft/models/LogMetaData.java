package com.github.wenweihu86.raft.models;

import com.goterl.lazycode.lazysodium.interfaces.Base;

public class LogMetaData extends BaseRequest {
    Long currentTerm  = 0l;
    String votedFor ;

    Long firstLogIndex  = 0l;
    Long commitIndex = 0l ;
    public LogMetaData() {

    }
    public LogMetaData(LogMetaData metaData) {
        this.currentTerm = metaData.currentTerm;
        this.votedFor = metaData.votedFor;
        this.firstLogIndex = metaData.firstLogIndex;
        this.commitIndex = metaData.commitIndex;
    }

    public Long getCurrentTerm() {
        return currentTerm;
    }

    public LogMetaData setCurrentTerm(Long currentTerm) {
        this.currentTerm = currentTerm;
        return this;
    }

    public String getVotedFor() {
        return votedFor;
    }

    public LogMetaData setVotedFor(String votedFor) {
        this.votedFor = votedFor;
        return  this;
    }

    public Long getFirstLogIndex() {
        return firstLogIndex;
    }

    public LogMetaData setFirstLogIndex(Long firstLogIndex) {
        this.firstLogIndex = firstLogIndex;
        return  this;
    }

    public Long getCommitIndex() {
        return commitIndex;
    }

    public LogMetaData setCommitIndex(Long commitIndex) {
        this.commitIndex = commitIndex;
        return  this;
    }

}
