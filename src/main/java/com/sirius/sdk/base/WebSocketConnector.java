package com.sirius.sdk.base;

import com.neovisionaries.ws.client.*;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.StringUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
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
    byte[] credentials = null;
    WebSocket webSocket;

    PublishSubject<byte[]> publishSubject = PublishSubject.create();

    @Override
    public Observable<byte[]> listen() {
        return publishSubject;
    }

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
        public void onTextFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) {
            if (webSocketFrame != null) {
                publishSubject.onNext(webSocketFrame.getPayload());
            }
        }

        @Override
        public void onBinaryFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) {
            if (webSocketFrame != null) {
                publishSubject.onNext(webSocketFrame.getPayload());
            }
        }
    };


    public void initWebSocket() {
        String url = serverAddress + "/" + path;
        while (url.endsWith("/"))
            url = url.substring(0, url.length()-1);
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
