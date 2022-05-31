package com.github.wenweihu86.raft.modelsExample;

public class SetResponse {
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    Boolean success;

    @Override
    public String toString() {
        return "SetResponse{" +
                "success=" + success +
                '}';
    }
}
