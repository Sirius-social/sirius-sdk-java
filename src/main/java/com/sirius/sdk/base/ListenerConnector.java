package com.sirius.sdk.base;

import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.StringUtils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import sun.nio.cs.UTF_8;

public class ListenerConnector extends BaseConnector {


    Logger log = Logger.getLogger(ListenerConnector.class.getName());
    public int defTimeout = 30;
    Charset encoding = StandardCharsets.UTF_8;
    String serverAddress;
    String path;
    byte[] credentials;
   // WebSocket webSocket;

    public ListenerConnector(int defTimeout, Charset encoding, String serverAddress, String path, byte[] credentials) {
        this.defTimeout = defTimeout;
        this.encoding = encoding;
        this.serverAddress = serverAddress;
        this.path = path;
        this.credentials = credentials;
        initListener();
    }

    public ListenerConnector(String serverAddress, String path, byte[] credentials) {
        this.serverAddress = serverAddress;
        this.path = path;
        this.credentials = credentials;
        initListener();
    }



    public void initListener() {
        String url = serverAddress + "/" + path;
       /* try {
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
        }*/
    }



    public void onTextFrame(String text) throws Exception {
        onBinaryFrame(text.getBytes(StandardCharsets.UTF_8));
    }


    public void onBinaryFrame(byte[] byteArray) throws Exception {
        read(byteArray, defTimeout);
    }

    public boolean isOpen() {
        return true;
    }

    @Override
    public void open() {
       /* if (!isOpen()) {
            try {
                webSocket.connect();
            } catch (WebSocketException e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public void close() {
        /*if (isOpen()) {
            webSocket.disconnect();
        }*/
    }

    CompletableFuture<byte[]> readFuture = new CompletableFuture<>();

    @Override
    public CompletableFuture<byte[]> read() {
        readFuture = new CompletableFuture<>();
        return readFuture;
    }


    private byte[] read(byte[] byteArray, int timeout) {

        if (byteArray != null) {
            readFuture.complete(byteArray);
            return byteArray;
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
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(serverAddress);
        List <NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("name", new String(data, StandardCharsets.UTF_8)));
        //nvps.add(new BasicNameValuePair("password", "secret"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        CloseableHttpResponse response2 = null;
        try {
            response2 = httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println(response2.getStatusLine());
            HttpEntity entity2 = response2.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            try {
                EntityUtils.consume(entity2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                response2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public boolean write(Message message) {
        String payload = message.serialize();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(serverAddress);
        List <NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("name", payload));
        //nvps.add(new BasicNameValuePair("password", "secret"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        CloseableHttpResponse response2 = null;
        try {
            response2 = httpclient.execute(httpPost);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            System.out.println(response2.getStatusLine());
            HttpEntity entity2 = response2.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            try {
                EntityUtils.consume(entity2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                response2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
