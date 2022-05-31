package com.github.wenweihu86.raft.models;

import com.sirius.sdk.utils.GsonUtils;

public class BaseResponse {

    public String tosGson() {
        return GsonUtils.getDefaultGson().toJson(this);
    }

}
