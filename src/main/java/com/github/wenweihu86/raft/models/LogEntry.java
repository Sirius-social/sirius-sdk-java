package com.github.wenweihu86.raft.models;

public class LogEntry {
    Long term ;
     Long index ;
     EntryType type ;
    byte[] data ;

    public LogEntry( ) {

    }
    public LogEntry(LogEntry entry) {
        this.term = entry.term;
        this.index = entry.index;
        this.type = entry.type;
    }

    public Long getTerm() {
        return term;
    }

    public LogEntry setTerm(Long term) {
        this.term = term;
        return this;
    }

    public Long getIndex() {
        return index;
    }

    public LogEntry setIndex(Long index) {
        this.index = index;
        return this;
    }

    public EntryType getType() {
        return type;
    }

    public LogEntry setType(EntryType type) {
        this.type = type;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public LogEntry setData(byte[] data) {
        this.data = data;
        return this;
    }


}
