package com.github.wenweihu86.raft.models;

public class SnapshotMetaData {
    Long lastIncludedIndex = 0l;

    Long lastIncludedTerm = 0l;
    public Long getLastIncludedIndex() {
        return lastIncludedIndex;
    }

    public SnapshotMetaData setLastIncludedIndex(Long lastIncludedIndex) {
        this.lastIncludedIndex = lastIncludedIndex;
        return  this;
    }

    public Long getLastIncludedTerm() {
        return lastIncludedTerm;
    }

    public SnapshotMetaData setLastIncludedTerm(Long lastIncludedTerm) {
        this.lastIncludedTerm = lastIncludedTerm;
        return this;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public SnapshotMetaData setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    Configuration configuration;
}
