package com.github.wenweihu86.raft.models;

import java.util.List;

public class AppendEntriesRequest  extends  BaseRequest{
     String serverId ;
    Long term ;
    Long prevLogIndex ;
    Long prevLogTerm ;

    Long commitIndex ;
    List<LogEntry> entries ;
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public Long getTerm() {
        return term;
    }

    public void setTerm(Long term) {
        this.term = term;
    }

    public Long getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(Long prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public Long getPrevLogTerm() {
        return prevLogTerm;
    }

    public void setPrevLogTerm(Long prevLogTerm) {
        this.prevLogTerm = prevLogTerm;
    }

    public Long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(Long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public List<LogEntry> getEntriesList() {
        return entries;
    }

    public void setEntries(List<LogEntry> entries) {
        this.entries = entries;
    }

    public int getEntriesCount() {
        if(entries!=null){
            return entries.size();
        }
       return 0;
    }

    public void addEntries(LogEntry entry) {
        entries.add(entry);
    }

    @Override
    public String toString() {
        return "AppendEntriesRequest{" +
                "serverId=" + serverId +
                ", term=" + term +
                ", prevLogIndex=" + prevLogIndex +
                ", prevLogTerm=" + prevLogTerm +
                ", commitIndex=" + commitIndex +
                ", entries=" + entries +
                '}';
    }
}
