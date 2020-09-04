package com.sirius.sdk.agent.wallet.impl;

import com.sirius.sdk.agent.RemoteParams;

public interface RemoteCall<T> {
    T remoteCall (String type, RemoteParams.RemoteParamsBuilder params);
}
