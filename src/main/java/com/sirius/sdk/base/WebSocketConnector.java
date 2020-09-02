package com.sirius.sdk.base;

import com.neovisionaries.ws.client.*;
import com.sirius.sdk.errors.sirius_exceptions.SiriusConnectionClosed;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WebSocketConnector extends BaseConnector {

    public int defTimeout = 30;
    Charset encoding = StandardCharsets.UTF_8;
    String serverAddress;
    String path;
    byte[] credentials;
    WebSocket webSocket;

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

        }

        @Override
        public void onConnectError(WebSocket webSocket, WebSocketException e) throws Exception {

        }

        @Override
        public void onDisconnected(WebSocket webSocket, WebSocketFrame webSocketFrame, WebSocketFrame webSocketFrame1, boolean b) throws Exception {

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
           // System.out.println("onTextMessage1="+s);
        }

        @Override
        public void onTextMessage(WebSocket webSocket, byte[] bytes) throws Exception {
           // System.out.println("onTextMessage2="+new String(bytes, StandardCharsets.US_ASCII));
        }

        @Override
        public void onBinaryMessage(WebSocket webSocket, byte[] bytes) throws Exception {
          //  System.out.println("onBinaryMessage="+new String(bytes, StandardCharsets.US_ASCII));
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
        try {
            webSocket = new WebSocketFactory()
                    .setVerifyHostname(false)
                    .setConnectionTimeout(defTimeout * 1000)
                    .createSocket(url)
                    .addListener(webSocketListener)
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .setPingInterval(60 * 3 * 1000).
                            addHeader("origin", serverAddress).
                            addHeader("credentials", StringUtils.bytesToString(credentials));

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
    public byte[] read(int timeout) {
        readFuture = new CompletableFuture<>();
        try {
            return readFuture.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }


    private byte[] read(WebSocketFrame frame, WebSocketException exception, int timeout) {
        System.out.println("read="+frame.getPayloadText());
        if (exception != null) {
            //  throw  new SiriusConnectionClosed();
        }
        if (frame != null) {
            readFuture.complete(frame.getPayload());
            return frame.getPayload();
        }
        return null;
   /*     try:
        msg = await self._ws.receive(timeout=timeout)
        except asyncio.TimeoutError as e:
        raise SiriusTimeoutIO() from e
        if msg.type in [aiohttp.WSMsgType.CLOSE, aiohttp.WSMsgType.CLOSING, aiohttp.WSMsgType.CLOSED]:
        raise SiriusConnectionClosed()
        elif msg.type == aiohttp.WSMsgType.TEXT:
        return msg.data.encode(self.ENC)
        elif msg.type == aiohttp.WSMsgType.BINARY:
        return msg.data
        elif msg.type == aiohttp.WSMsgType.ERROR:
        raise SiriusIOError()*/
    }


    @Override
    public boolean write(byte[] data) {
        webSocket.sendBinary(data);
        return true;
    }

    public boolean write(Message message) {
        String payload = message.serialize();
        webSocket.sendText(payload);
        return true;
    }
}


  /*  WebSocket ws;
    private static final int TIMEOUT = 15000;

    private void connect() {

    }*/

/*
    DEF_TIMEOUT = 30.0
            ENC = 'utf-8'

            def __init__(
            self, server_address: str, path: str, credentials: bytes,
            timeout: float=DEF_TIMEOUT, loop: asyncio.AbstractEventLoop=None
            ):
            self.__session = aiohttp.ClientSession(
            loop=loop,
            timeout=aiohttp.ClientTimeout(total=timeout),
            headers={
            'origin': server_address,
            'credentials': credentials.decode('ascii')
            }
            )
            self._url = urljoin(server_address, path)
            self._ws = None

@property
    def is_open(self):
            return self._ws is not None

            async def open(self):
        if not self.is_open:
        self._ws = await self.__session.ws_connect(url=self._url)

        async def close(self):
        if not self.is_open:
        await self._ws.close()
        self._ws = None

        async def read(self, timeout: int=None) -> bytes:
        try:
        msg = await self._ws.receive(timeout=timeout)
        except asyncio.TimeoutError as e:
        raise SiriusTimeoutIO() from e
        if msg.type in [aiohttp.WSMsgType.CLOSE, aiohttp.WSMsgType.CLOSING, aiohttp.WSMsgType.CLOSED]:
        raise SiriusConnectionClosed()
        elif msg.type == aiohttp.WSMsgType.TEXT:
        return msg.data.encode(self.ENC)
        elif msg.type == aiohttp.WSMsgType.BINARY:
        return msg.data
        elif msg.type == aiohttp.WSMsgType.ERROR:
        raise SiriusIOError()

        async def write(self, message: Union[Message, bytes]) -> bool:
        if isinstance(message, Message):
        payload = message.serialize().encode(self.ENC)
        else:
        payload = message
        await self._ws.send_bytes(payload)
        return True*/
