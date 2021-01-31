package com.sirius.sdk.agent;

import com.sirius.sdk.base.WebSocketConnector;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.SiriusFieldTypeError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusFieldValueError;
import com.sirius.sdk.messaging.Message;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class BaseAgentConnection {

    public static final int IO_TIMEOUT = 30;
    String MSG_TYPE_CONTEXT = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/context";

    String serverAddress;
    byte[] credentials;
    P2PConnection p2p;

    int timeout = IO_TIMEOUT;
    WebSocketConnector connector;

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
        connector = new WebSocketConnector(IO_TIMEOUT, StandardCharsets.UTF_8, serverAddress, path(), credentials);
    }

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
        connector.open();
        byte[] payload = new byte[0];
        try {
            payload = connector.read().get(getTimeout(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        Message context = new Message(new String(payload, StandardCharsets.UTF_8));
        if (context.getType()==null){
            throw new SiriusFieldValueError("message @type is empty");
        }
        if(!MSG_TYPE_CONTEXT.equals(context.getType())){
            throw new SiriusFieldValueError("message @type not equal "+MSG_TYPE_CONTEXT);
        }
        setup(context);
    }
}
