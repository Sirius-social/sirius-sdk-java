package com.sirius.sdk.agent.connections;

import com.sirius.sdk.agent.RemoteParams;

public interface RemoteCall<T> {
    T remoteCall (String type, RemoteParams.RemoteParamsBuilder params);
}
