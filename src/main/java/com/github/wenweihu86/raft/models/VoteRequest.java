package com.github.wenweihu86.raft.models;

import com.sirius.sdk.agent.pairwise.Pairwise;

public class VoteRequest extends BaseRequest  {
    public String serverId ; // 请求选票的候选人的 Id

    public   Long term ; // 候选人的任期号

    public String getServerId() {
        return serverId;
    }

    public VoteRequest setServerId(String serverId) {
        this.serverId = serverId;
        return this;
    }
    public VoteRequest setServer(Server server) {
        this.server = server;
        return this;
    }

    public Long getTerm() {
        return term;
    }

    public VoteRequest setTerm(Long term) {
        this.term = term;
        return this;
    }
    public VoteRequest setPairwise(Pairwise pairwise) {
        this.pairwise = pairwise;
        return this;
    }
    public transient Pairwise pairwise;

    public Long getLastLogTerm() {
        return lastLogTerm;
    }

    public VoteRequest setLastLogTerm(Long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
        return this;
    }

    public Long getLastLogIndex() {
        return lastLogIndex;
    }

    public VoteRequest setLastLogIndex(Long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
        return this;
    }

    public Long lastLogTerm ; // 候选人的最后日志条目的任期号
    public Long lastLogIndex ; // 候选人最后日志条目的索引值

    @Override
    public String toString() {
        return "VoteRequest{" +
                "serverId=" + serverId +
                ", term=" + term +
                ", lastLogTerm=" + lastLogTerm +
                ", lastLogIndex=" + lastLogIndex +
                '}';
    }
}
