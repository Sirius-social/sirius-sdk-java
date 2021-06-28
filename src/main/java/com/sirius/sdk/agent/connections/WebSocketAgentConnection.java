package com.sirius.sdk.agent.connections;

import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.base.BaseConnector;
import com.sirius.sdk.base.WebSocketConnector;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusFieldValueError;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.rpc.AddressedTunnel;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public abstract class WebSocketAgentConnection extends BaseAgentConnection {

    public WebSocketAgentConnection(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout) {
        super(serverAddress, credentials, p2p, timeout);
    }

    @Override
    public BaseConnector createConnector() {
        return new WebSocketConnector(this.timeout, StandardCharsets.UTF_8, serverAddress, path(), credentials);
    }

}
