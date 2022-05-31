package com.github.wenweihu86.raft;

import com.github.wenweihu86.raft.models.Server;
import com.github.wenweihu86.raft.service.RaftConsensusServiceAsync;
import com.sirius.sdk.agent.CloudAgent;

/**
 * Created by wenweihu86 on 2017/5/5.
 */
public class Peer {
    private Server server;
    private CloudAgent rpcClient;
    private String rpcClientVerkey;
    private RaftConsensusServiceAsync raftConsensusServiceAsync;

    private long nextIndex;

    private long matchIndex;
    private volatile Boolean voteGranted;
    private volatile boolean isCatchUp;

    public Peer(Server server) {
        this.server = server;
        this.rpcClient = server.getCloudAgent();
        raftConsensusServiceAsync = new RaftConsensusAsyncCloudAgent(server);
        isCatchUp = false;
    }

    public Server getServer() {
        return server;
    }

    public CloudAgent getRpcClient() {
        return rpcClient;
    }

    public RaftConsensusServiceAsync getRaftConsensusServiceAsync() {
        return raftConsensusServiceAsync;
    }

    public long getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(long nextIndex) {
        this.nextIndex = nextIndex;
    }

    public long getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(long matchIndex) {
        this.matchIndex = matchIndex;
    }

    public Boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(Boolean voteGranted) {
        this.voteGranted = voteGranted;
    }


    public boolean isCatchUp() {
        return isCatchUp;
    }

    public void setCatchUp(boolean catchUp) {
        isCatchUp = catchUp;
    }
}
