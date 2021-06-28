package com.sirius.sdk.agent.connections;

import com.sirius.sdk.agent.RemoteParams;
import com.sirius.sdk.base.BaseConnector;
import com.sirius.sdk.base.WebSocketConnector;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusConnectionClosed;
import com.sirius.sdk.errors.sirius_exceptions.SiriusFieldTypeError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusFieldValueError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusRPCError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusTimeoutRPC;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.messaging.Type;
import com.sirius.sdk.rpc.AddressedTunnel;
import com.sirius.sdk.rpc.Future;
import com.sirius.sdk.rpc.Parsing;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseAgentConnection {
    Logger log = Logger.getLogger(AddressedTunnel.class.getName());

    public static final int IO_TIMEOUT = 30;
    String MSG_TYPE_CONTEXT = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/context";

    String serverAddress;
    byte[] credentials;
    P2PConnection p2p;

    int timeout = IO_TIMEOUT;
    BaseConnector connector;

    public String getServerAddress() {
        return serverAddress;
    }

    public byte[] getCredentials() {
        return credentials;
    }

    public BaseConnector getConnector() {
        return connector;
    }

    public P2PConnection getP2p() {
        return p2p;
    }

    public void setTimeout(int timeout) {
        if (timeout <= 0) {
            throw new RuntimeException("Timeout must be > 0");
        }
        this.timeout = timeout;
    }


    public BaseAgentConnection(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout) {
        this.serverAddress = serverAddress;
        this.credentials = credentials;
        this.p2p = p2p;
        this.timeout = timeout;
        connector = createConnector();
    }

    public  abstract BaseConnector createConnector();
    public abstract String path();

    public void setup(Message context) {

    }

    public int getTimeout() {
        return timeout;
    }


    public boolean isOpen() {
        return connector.isOpen();
    }

    public void close() {
        connector.close();
    }

    public void create() throws SiriusFieldValueError {
        CompletableFuture<byte[]> feat = connector.read();
        connector.open();
        byte[] payload = new byte[0];
        try {
            payload = feat.get(getTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
        String msgString = new String(payload, StandardCharsets.UTF_8);
        //log.log(Level.INFO, "Received message: " + msgString);
        Message context = new Message(msgString);
        if (context.getType()==null){
            throw new SiriusFieldValueError("message @type is empty");
        }
        if(!MSG_TYPE_CONTEXT.equals(context.getType())){
            throw new SiriusFieldValueError("message @type not equal "+MSG_TYPE_CONTEXT);
        }
        setup(context);
    }

    /**
     * Call Agent services
     *
     * @param msgType
     * @param params
     * @param waitResponse wait for response
     * @return
     */
    public abstract  Object remoteCall(String msgType, RemoteParams params, boolean waitResponse) throws Exception;

    public  Object remoteCall(String msgType,  RemoteParams params)
            throws Exception {
        return remoteCall(msgType, params, true);
    }

    public Object remoteCall(String msgType)
            throws Exception {
        return remoteCall(msgType, null);
    }
}
