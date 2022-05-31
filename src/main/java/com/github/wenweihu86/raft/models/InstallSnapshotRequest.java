package com.github.wenweihu86.raft.models;

import java.util.Arrays;

public class InstallSnapshotRequest extends BaseRequest {
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


    public SnapshotMetaData getSnapshotMetaData() {
        return snapshotMetaData;
    }

    public void setSnapshotMetaData(SnapshotMetaData snapshotMetaData) {
        this.snapshotMetaData = snapshotMetaData;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Boolean getIsFirst() {
        return isFirst;
    }

    public void setIsFirst(Boolean first) {
        isFirst = first;
    }

    public Boolean getIsLast() {
        return isLast;
    }

    public void setIsLast(Boolean last) {
        isLast = last;
    }

    String serverId;
    Long term ;
    transient SnapshotMetaData snapshotMetaData ;
    String fileName ;
    Long offset ;
    byte[] data;
    Boolean isFirst;
    Boolean isLast;

    @Override
    public String toString() {
        return "InstallSnapshotRequest{" +
                "serverId=" + serverId +
                ", term=" + term +
                ", snapshotMetaData=" + snapshotMetaData +
                ", fileName='" + fileName + '\'' +
                ", offset=" + offset +
                ", data=" + Arrays.toString(data) +
                ", isFirst=" + isFirst +
                ", isLast=" + isLast +
                '}';
    }

}
