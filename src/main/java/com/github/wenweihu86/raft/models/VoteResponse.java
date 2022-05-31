package com.github.wenweihu86.raft.models;

public class VoteResponse extends BaseResponse {
     Long term ; // 当前任期号，以便于候选人去更新自己的任期号

     public Long getTerm() {
          return term;
     }

     public void setTerm(Long term) {
          this.term = term;
     }

     public Boolean getGranted() {
          return granted;
     }

     public void setGranted(Boolean granted) {
          this.granted = granted;
     }

     Boolean granted ; // 候选人赢得了此张选票时为真


}
