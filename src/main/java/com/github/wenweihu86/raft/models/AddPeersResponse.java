package com.github.wenweihu86.raft.models;

public class AddPeersResponse extends BaseResponse {
     ResCode resCode ;

     public ResCode getResCode() {
          return resCode;
     }

     public void setResCode(ResCode resCode) {
          this.resCode = resCode;
     }

     public String getResMsg() {
          return resMsg;
     }

     public void setResMsg(String resMsg) {
          this.resMsg = resMsg;
     }

     String resMsg ;

     @Override
     public String toString() {
          return "AddPeersResponse{" +
                  "resCode=" + resCode +
                  ", resMsg='" + resMsg + '\'' +
                  '}';
     }
}
