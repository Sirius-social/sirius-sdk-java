package com.sirius.sdk.base;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.sirius.sdk.errors.sirius_exceptions.SiriusConnectionClosed;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebSocketConnector extends BaseConnector {

    Logger log = Logger.getLogger(WebSocketConnector.class.getName());
    public int defTimeout = 30;
    Charset encoding = StandardCharsets.UTF_8;
    String serverAddress;
    String path;
    byte[] credentials;
    WebSocket webSocket;

    public Function<byte[], Void> readCallback = null;

    public WebSocketConnector(int defTimeout, Charset encoding, String serverAddress, String path, byte[] credentials) {
        this.defTimeout = defTimeout;
        this.encoding = encoding;
        this.serverAddress = serverAddress;
        this.path = path;
        this.credentials = credentials;
        initWebSocket();
    }

    public WebSocketConnector(String serverAddress, String path, byte[] credentials) {
        this.serverAddress = serverAddress;
        this.path = path;
        this.credentials = credentials;
        initWebSocket();
    }

    WebSocketListener webSocketListener = new WebSocketAdapter() {

        @Override
        public void onConnectError(WebSocket webSocket, WebSocketException e) {
            log.log(Level.WARNING, "Connect error");
        }

        @Override
        public void onDisconnected(WebSocket websocket,
                                   WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame,
                                   boolean closedByServer) throws Exception {
            if (closedByServer) {
                throw new SiriusConnectionClosed("Socket is closed by server");
            }
            //log.log(Level.INFO, "Disconnected");
        }

        @Override
        public void onTextFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) {
            read(webSocketFrame);
        }

        @Override
        public void onBinaryFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) {
            read(webSocketFrame);
        }

        @Override
        public void handleCallbackError(WebSocket webSocket, Throwable throwable) throws Exception {
            if (throwable instanceof SiriusConnectionClosed) {
                log.log(Level.WARNING, throwable.getMessage());
                Retryer retryer = Retryer.builder()
                        .maxAttempts(3)
                        .waitDuration(Duration.ofMillis(100L))
                        .build();
                retryer.retry(() -> {
                    WebSocketConnector.this.close();
                    WebSocketConnector.this.initWebSocket();
                    return null;
                });
            }
        }
    };


    public void initWebSocket() {
        String url = serverAddress + "/" + path;
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        try {
            webSocket = new WebSocketFactory()
                    .setVerifyHostname(false)
                    .setConnectionTimeout(defTimeout * 1000)
                    .createSocket(url)
                    .addListener(webSocketListener)
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .setPingInterval(60 * 3 * 1000).
                    addHeader("origin", serverAddress);
            if (this.credentials != null) {
                webSocket.addHeader("credentials", StringUtils.bytesToString(credentials));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean isOpen() {
        if (webSocket != null) {
            return webSocket.isOpen();
        }
        return false;
    }

    @Override
    public void open() {
        if (!isOpen()) {
            try {
                webSocket.connect();
            } catch (WebSocketException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        if (isOpen()) {
            webSocket.disconnect();
        }
    }

    CompletableFuture<byte[]> readFuture = new CompletableFuture<>();

    @Override
    public CompletableFuture<byte[]> read() {
        readFuture = new CompletableFuture<>();
        return readFuture;
    }


    private byte[] read(WebSocketFrame frame) {
        if (frame != null) {
            readFuture.complete(frame.getPayload());
            if (readCallback != null) {
                readCallback.apply(frame.getPayload());
            }
            return frame.getPayload();
        }
        return null;
    }


    @Override
    public boolean write(byte[] data) {
        //log.log(Level.INFO, "Sending binary data");
        webSocket.sendBinary(data);
        return true;
    }

    public boolean write(Message message) {
        String payload = message.serialize();
        //log.log(Level.INFO, "Sending message");
        webSocket.sendText(payload);
        return true;
    }
}
