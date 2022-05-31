package com.github.wenweihu86.raft.service;

import com.github.wenweihu86.raft.models.*;


/**
 * raft节点之间相互通信的接口。
 * Created by wenweihu86 on 2017/5/2.
 */
public interface RaftConsensusService {

    VoteResponse preVote(VoteRequest request);

    VoteResponse requestVote(VoteRequest request);

    AppendEntriesResponse appendEntries(AppendEntriesRequest request);

    InstallSnapshotResponse installSnapshot(InstallSnapshotRequest request);
}
