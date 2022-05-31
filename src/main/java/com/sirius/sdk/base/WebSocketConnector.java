package com.sirius.sdk.base;

import com.neovisionaries.ws.client.*;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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

    WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onStateChanged(WebSocket webSocket, WebSocketState webSocketState) throws Exception {

        }

        @Override
        public void onConnected(WebSocket webSocket, Map<String, List<String>> map) throws Exception {
            //log.log(Level.INFO, "Connected");
        }

        @Override
        public void onConnectError(WebSocket webSocket, WebSocketException e) throws Exception {
            log.log(Level.WARNING, "Connect error");
        }

        @Override
        public void onDisconnected(WebSocket webSocket, WebSocketFrame webSocketFrame, WebSocketFrame webSocketFrame1, boolean b) throws Exception {
            //log.log(Level.INFO, "Disconnected");
        }

        @Override
        public void onFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onContinuationFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onTextFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
            read(webSocketFrame, null, defTimeout);
        }

        @Override
        public void onBinaryFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {
            read(webSocketFrame, null, defTimeout);
        }

        @Override
        public void onCloseFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onPingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onPongFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onTextMessage(WebSocket webSocket, String s) throws Exception {

        }

        @Override
        public void onTextMessage(WebSocket webSocket, byte[] bytes) throws Exception {

        }

        @Override
        public void onBinaryMessage(WebSocket webSocket, byte[] bytes) throws Exception {

        }

        @Override
        public void onSendingFrame(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onFrameSent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onFrameUnsent(WebSocket webSocket, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onThreadCreated(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {

        }

        @Override
        public void onThreadStarted(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {

        }

        @Override
        public void onThreadStopping(WebSocket webSocket, ThreadType threadType, Thread thread) throws Exception {

        }

        @Override
        public void onError(WebSocket webSocket, WebSocketException e) throws Exception {

        }

        @Override
        public void onFrameError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onMessageError(WebSocket webSocket, WebSocketException e, List<WebSocketFrame> list) throws Exception {

        }

        @Override
        public void onMessageDecompressionError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {

        }

        @Override
        public void onTextMessageError(WebSocket webSocket, WebSocketException e, byte[] bytes) throws Exception {

        }

        @Override
        public void onSendError(WebSocket webSocket, WebSocketException e, WebSocketFrame webSocketFrame) throws Exception {

        }

        @Override
        public void onUnexpectedError(WebSocket webSocket, WebSocketException e) throws Exception {

        }

        @Override
        public void handleCallbackError(WebSocket webSocket, Throwable throwable) throws Exception {

        }

        @Override
        public void onSendingHandshake(WebSocket webSocket, String s, List<String[]> list) throws Exception {

        }
    };


    public void initWebSocket() {
        String url = serverAddress + "/" + path;
        while (url.endsWith("/"))
            url = url.substring(0, url.length()-1);
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
        // Install the all-trusting trust manager
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            /*okHttpClient.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            okHttpClient.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });*/

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        try {
            webSocket = new WebSocketFactory()
                    .setVerifyHostname(false)
                    .setSSLContext(sslContext)
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


    private byte[] read(WebSocketFrame frame, WebSocketException exception, int timeout) {
        if (frame != null) {
            readFuture.complete(frame.getPayload());
          //  System.out.println("read="+new String(frame.getPayload()));
            if (readCallback != null)
                readCallback.apply(frame.getPayload());
            return frame.getPayload();
        }
        return null;
    }


    @Override
    public boolean write(byte[] data) {
        //    log.log(Level.INFO, "Sending binary data="+new String(data));
        webSocket.sendBinary(data);
        return true;
    }

    public boolean write(Message message) {
        String payload = message.serialize();
     //   log.log(Level.INFO, "Sending message payload="+payload);
        webSocket.sendText(payload);
        return true;
    }
}
