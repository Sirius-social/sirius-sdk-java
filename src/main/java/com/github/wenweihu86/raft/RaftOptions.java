package com.github.wenweihu86.raft;



/**

 * Created by wenweihu86 on 2017/5/2.
 */

public class RaftOptions {

    public int getElectionTimeoutMilliseconds() {
        return electionTimeoutMilliseconds;
    }

    public void setElectionTimeoutMilliseconds(int electionTimeoutMilliseconds) {
        this.electionTimeoutMilliseconds = electionTimeoutMilliseconds;
    }

    public int getHeartbeatPeriodMilliseconds() {
        return heartbeatPeriodMilliseconds;
    }

    public void setHeartbeatPeriodMilliseconds(int heartbeatPeriodMilliseconds) {
        this.heartbeatPeriodMilliseconds = heartbeatPeriodMilliseconds;
    }

    public int getSnapshotPeriodSeconds() {
        return snapshotPeriodSeconds;
    }

    public void setSnapshotPeriodSeconds(int snapshotPeriodSeconds) {
        this.snapshotPeriodSeconds = snapshotPeriodSeconds;
    }

    public int getSnapshotMinLogSize() {
        return snapshotMinLogSize;
    }

    public void setSnapshotMinLogSize(int snapshotMinLogSize) {
        this.snapshotMinLogSize = snapshotMinLogSize;
    }

    public int getMaxSnapshotBytesPerRequest() {
        return maxSnapshotBytesPerRequest;
    }

    public void setMaxSnapshotBytesPerRequest(int maxSnapshotBytesPerRequest) {
        this.maxSnapshotBytesPerRequest = maxSnapshotBytesPerRequest;
    }

    public int getMaxLogEntriesPerRequest() {
        return maxLogEntriesPerRequest;
    }

    public void setMaxLogEntriesPerRequest(int maxLogEntriesPerRequest) {
        this.maxLogEntriesPerRequest = maxLogEntriesPerRequest;
    }

    public int getMaxSegmentFileSize() {
        return maxSegmentFileSize;
    }

    public void setMaxSegmentFileSize(int maxSegmentFileSize) {
        this.maxSegmentFileSize = maxSegmentFileSize;
    }

    public long getCatchupMargin() {
        return catchupMargin;
    }

    public void setCatchupMargin(long catchupMargin) {
        this.catchupMargin = catchupMargin;
    }

    public long getMaxAwaitTimeout() {
        return maxAwaitTimeout;
    }

    public void setMaxAwaitTimeout(long maxAwaitTimeout) {
        this.maxAwaitTimeout = maxAwaitTimeout;
    }

    public int getRaftConsensusThreadNum() {
        return raftConsensusThreadNum;
    }

    public void setRaftConsensusThreadNum(int raftConsensusThreadNum) {
        this.raftConsensusThreadNum = raftConsensusThreadNum;
    }

    public boolean isAsyncWrite() {
        return asyncWrite;
    }

    public void setAsyncWrite(boolean asyncWrite) {
        this.asyncWrite = asyncWrite;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    // A follower would become a candidate if it doesn't receive any message
    // from the leader in electionTimeoutMs milliseconds
    private int electionTimeoutMilliseconds = 5000;

    // A leader sends RPCs at least this often, even if there is no data to send
    private int heartbeatPeriodMilliseconds = 500;


    private int snapshotPeriodSeconds = 3600;

    private int snapshotMinLogSize = 100 * 1024 * 1024;
    private int maxSnapshotBytesPerRequest = 500 * 1024; // 500k

    private int maxLogEntriesPerRequest = 5000;


    private int maxSegmentFileSize = 100 * 1000 * 1000;


    private long catchupMargin = 500;


    private long maxAwaitTimeout = 1000;


    private int raftConsensusThreadNum = 20;


    private boolean asyncWrite = false;


    private String dataDir = System.getProperty("com.github.wenweihu86.raft.data.dir");
}
