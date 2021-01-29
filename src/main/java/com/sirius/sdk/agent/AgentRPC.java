package com.sirius.sdk.agent;

import com.neovisionaries.ws.client.WebSocket;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.errors.sirius_exceptions.*;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.messaging.Type;
import com.sirius.sdk.rpc.AddressedTunnel;
import com.sirius.sdk.rpc.Future;
import com.sirius.sdk.rpc.Parsing;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RPC service.
 * <p>
 * Proactive form of Smart-Contract design
 */
public class AgentRPC extends BaseAgentConnection {

    List<Endpoint> endpoints;
    List<String> networks;
    Map<String, WebSocket> websockets;
    boolean preferAgentSide;
    AddressedTunnel tunnelRpc;
    AddressedTunnel tunnelСoprotocols;

    public List<String> getNetworks() {
        return networks;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }


    public AgentRPC(String serverAddress, byte[] credentials, P2PConnection p2p, int timeout) {
        super(serverAddress, credentials, p2p, timeout);
        endpoints = new ArrayList<>();
        networks = new ArrayList<>();
        tunnelRpc = null;
        tunnelСoprotocols = null;
        websockets = new HashMap<>();
        preferAgentSide = true;

        //self.__connector = aiohttp.TCPConnector(verify_ssl = False, keepalive_timeout = 60)
    }

    @Override
    public String path() {
        return "rpc";
    }


    /**
     * Call Agent services
     *
     * @param msgType
     * @param params
     * @param waitResponse wait for response
     * @return
     */
    public Object remoteCall(String msgType,  RemoteParams params, boolean waitResponse)
            throws Exception {
        if (!connector.isOpen()) {
            throw new SiriusConnectionClosed("Open agent connection at first");
        }
        long expirationTime = 0;
        if (timeout != 0) {
            expirationTime = (System.currentTimeMillis() + (timeout * 1000)) / 1000;
        }

        Future future = new Future(tunnelRpc, expirationTime);
        Message request = Parsing.buildRequest(msgType, future, params);
        String payload = request.serialize();
        Type msgTyp = Type.fromStr(msgType);
        boolean isEncryptes = !"admin".equals(msgTyp.getProtocol()) && !"microledgers".equals(msgTyp.getProtocol());

        boolean isPosted = tunnelRpc.post(request, isEncryptes);
        if (!isPosted) {
            throw new SiriusRPCError();
        }
        if (waitResponse) {
            boolean success = future.waitPromise(timeout);
            if (success) {
                if (future.hasException()) {
                    future.raiseException();
                } else {
                    return future.getValue();
                }
            } else {
                throw new SiriusTimeoutRPC();
            }
        }
        return null;
    }

    public Object remoteCall(String msgType,  RemoteParams params)
            throws Exception {
        return remoteCall(msgType, params, true);
    }

    public Object remoteCall(String msgType)
            throws Exception {
        return remoteCall(msgType, null);
    }


    @Override
    public void setup(Message context) {
        super.setup(context);
        //Extract proxy info
        List<JSONObject> proxies = new ArrayList<>();
        JSONArray proxiesArray = context.getJSONArrayFromJSON("~proxy", null);
        if (proxiesArray != null) {
            for (int i = 0; i < proxiesArray.length(); i++) {
                proxies.add(proxiesArray.getJSONObject(i));
            }
        }
        String channel_rpc = null;
        String channel_sub_protocol = null;

        for (JSONObject proxy : proxies) {
            if ("reverse".equals(proxy.getString("id"))) {
                channel_rpc = proxy.getJSONObject("data").getJSONObject("json").getString("address");
            } else if ("sub-protocol".equals(proxy.getString("id"))) {
                channel_sub_protocol = proxy.getJSONObject("data").getJSONObject("json").getString("address");
            }
        }
        if (channel_rpc == null) {
            throw new RuntimeException("rpc channel is empty");
        }
        if (channel_sub_protocol == null) {
            throw new RuntimeException("sub-protocol channel is empty");
        }
        tunnelRpc = new AddressedTunnel(channel_rpc, connector, connector, p2p);
        tunnelСoprotocols = new AddressedTunnel(channel_sub_protocol, connector, connector, p2p);
        //Extract active endpoints
        JSONArray endpointsArray = context.getJSONArrayFromJSON("~endpoints", null);
        List<Endpoint> endpointsCollection = new ArrayList<>();
        if (endpointsArray != null) {
            for (int i = 0; i < endpointsArray.length(); i++) {
                JSONObject endpointObj = endpointsArray.getJSONObject(i);
                JSONObject bodyObj = endpointObj.getJSONObject("data").getJSONObject("json");
                String address = bodyObj.getString("address");
                String frontendKey = bodyObj.optString("frontend_routing_key");
                if (!frontendKey.isEmpty()) {
                    JSONArray routingKeys = bodyObj.getJSONArray("routing_keys");
                    if (routingKeys != null) {
                        for (int z = 0; z < routingKeys.length(); z++) {
                            JSONObject routingKey = routingKeys.getJSONObject(z);
                            boolean isDefault = routingKey.getBoolean("is_default");
                            String key = routingKey.getString("routing_key");
                            List<String> routingKeysList = new ArrayList<>();
                            routingKeysList.add(key);
                            routingKeysList.add(frontendKey);
                            endpointsCollection.add(new Endpoint(address, routingKeysList, isDefault));
                        }
                    }
                } else {
                    endpointsCollection.add(new Endpoint(address, new ArrayList<>(), false));
                }
            }
        }
        if (endpointsCollection.isEmpty()) {
            throw new RuntimeException("Endpoints are empty");
        }
        endpoints = endpointsCollection;
        //Extract Networks

        List<String> networkList = new ArrayList<>();

        JSONArray networksArray = context.getJSONArrayFromJSON("~networks", new JSONArray());
        for (int i = 0; i < networksArray.length(); i++) {
            String network = networksArray.getString(i);
            networkList.add(network);
        }
        networks = networkList;


    }

    /**
     * Send Message to other Indy compatible agent
     *
     * @param message     message
     * @param their_vk    Verkey of recipients
     * @param endpoint    Endpoint Address of recipient
     * @param myVk        Verkey of sender (None for anocrypt mode)
     * @param routingKeys Routing keys if it is exists
     * @param coprotocol  True if message is part of co-protocol stream
     *                    See:
     *                    - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0003-protocols
     *                    - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0008-message-id-and-threading
     * @return Response message if coprotocol is True
     */
    public Message sendMessage(Message message, List<String> their_vk, String endpoint,
                               String myVk, List<String> routingKeys, boolean coprotocol) throws SiriusConnectionClosed, SiriusRPCError {
        if(!connector.isOpen()){
            throw  new SiriusConnectionClosed("Open agent connection at first");
        }
        RemoteParams.RemoteParamsBuilder paramsBuilder = RemoteParams.RemoteParamsBuilder.create()
                .add("message",message);
        if(routingKeys ==null){
            routingKeys = new ArrayList<>();
        }
        paramsBuilder.add("routing_keys",routingKeys)
                .add("recipient_verkeys",their_vk)
                .add("sender_verkey",myVk);

        Object response = null;
        if (preferAgentSide) {
            paramsBuilder.add("timeout",timeout);
            paramsBuilder.add("endpoint_address",endpoint);
            try {
               response = remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/send_message",paramsBuilder.build());

            } catch (Exception siriusRPCError) {
                siriusRPCError.printStackTrace();
            }
        } else {
            try {
                response = remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prepare_message_for_send",paramsBuilder.build());
            /*    wired = await self.remote_call(
                        msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prepare_message_for_send',
                        params=params
                )
                if endpoint.startswith('ws://') or endpoint.startswith('wss://'):
                     ws = await self.__get_websocket(endpoint)
                      await ws.send_bytes(wired)
                       ok, body = True, b''
                 else:
                     ok, body = await http_send(wired, endpoint, timeout=self.timeout, connector=self.__connector)
               body = body.decode()*/

            } catch (Exception siriusRPCError) {
                siriusRPCError.printStackTrace();
            }
        }

        boolean ok = (boolean) ((Pair) response).first;
        String body = (String) ((Pair) response).second;

        if (!ok) {
            throw new SiriusRPCError(body);
        }

        return null;
    }

    public void startProtocolWithThreads(List<String> threads, int timeToLiveSec) {
        try {
            this.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/stop_protocol",
                    RemoteParams.RemoteParamsBuilder.create()
                            .add("channel_address", this.tunnelСoprotocols.getAddress())
                            .add("ttl", timeToLiveSec).build());
        } catch (Exception siriusRPCError) {
            siriusRPCError.printStackTrace();
        }
    }

    public void stopProtocolWithThreads(List<String> threads, boolean offResponse) {
        try {
            this.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/stop_protocol",
                    RemoteParams.RemoteParamsBuilder.create()
                            .add("threads", threads)
                            .add("off_response", offResponse).build(),
                    !offResponse);
        } catch (Exception siriusRPCError) {
            siriusRPCError.printStackTrace();
        }
    }

    public void stopProtocolWithThreads(List<String> threads) {
        stopProtocolWithThreads(threads, false);
    }

    public void startProtocolForP2P(String senderVerkey, String recipientVerkey, List<String> protocols, int timeToLiveSec) {
        try {
            this.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/start_protocol",
                    RemoteParams.RemoteParamsBuilder.create()
                            .add("sender_verkey", senderVerkey)
                            .add("recipient_verkey", recipientVerkey)
                            .add("protocols", protocols)
                            .add("channel_address", this.tunnelСoprotocols.getAddress())
                            .add("ttl", timeToLiveSec).build());
        } catch (Exception siriusRPCError) {
            siriusRPCError.printStackTrace();
        }
    }

    public void stopProtocolForP2P(String senderVerkey, String recipientVerkey, List<String> protocols, boolean offResponse) {
        try {
            this.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/stop_protocol",
                    RemoteParams.RemoteParamsBuilder.create()
                            .add("sender_verkey", senderVerkey)
                            .add("recipient_verkey", recipientVerkey)
                            .add("protocols", protocols)
                            .add("off_response", offResponse).build(),
                    !offResponse);
        } catch (Exception siriusRPCError) {
            siriusRPCError.printStackTrace();
        }
    }

}
  /*  async def send_message(
            self, message: Message,
            their_vk: Union[List[str], str], endpoint: str,
            my_vk: Optional[str], routing_keys: Optional[List[str]],
            coprotocol: bool=False
    ) -> Optional[Message]:

            if not self._connector.is_open:
    raise SiriusConnectionClosed('Open agent connection at first')
        if isinstance(their_vk, str):
    recipient_verkeys = [their_vk]
            else:
    recipient_verkeys = their_vk
            params = {
            'message': message,
            'routing_keys': routing_keys or [],
            'recipient_verkeys': recipient_verkeys,
            'sender_verkey': my_vk
}
        if self.__prefer_agent_side:
                params['timeout'] = self.timeout
                params['endpoint_address'] = endpoint
                ok, body = await self.remote_call(
                msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/send_message',
                params=params
                )
                else:
                wired = await self.remote_call(
                msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prepare_message_for_send',
                params=params
                )
                if endpoint.startswith('ws://') or endpoint.startswith('wss://'):
                ws = await self.__get_websocket(endpoint)
                await ws.send_bytes(wired)
                ok, body = True, b''
                else:
                ok, body = await http_send(wired, endpoint, timeout=self.timeout, connector=self.__connector)
                body = body.decode()
                if not ok:
                raise SiriusRPCError(body)
                else:
                if coprotocol:
                response = await self.read_protocol_message()
                return response
                else:
                return None



                }*/
 /*"""
    """

         def __init__(self, *args, **kwargs):
         super().__init__(*args, **kwargs)
         self.__tunnel_rpc = None
         self.__tunnel_coprotocols = None
         self.__endpoints = []
         self.__networks = []
         self.__websockets = {}
         self.__prefer_agent_side = True
         self.__connector = aiohttp.TCPConnector(verify_ssl=False, keepalive_timeout=60)

@property
    def endpoints(self) -> List[Endpoint]:
            return self.__endpoints

@property
    def networks(self) -> List[str]:
            return self.__networks

            async def remote_call(self, msg_type: str, params: dict=None, wait_response: bool=True) -> Any:
            """Call Agent services

        :param msg_type:
        :param params:
        :param wait_response: wait for response
        :return:
        """
            if not self._connector.is_open:
            raise SiriusConnectionClosed('Open agent connection at first')
            if self._timeout:
            expiration_time = datetime.datetime.now() + datetime.timedelta(seconds=self._timeout)
            else:
            expiration_time = None
            future = Future(
            tunnel=self.__tunnel_rpc,
            expiration_time=expiration_time
            )
            request = build_request(
            msg_type=msg_type,
            future=future,
            params=params or {}
            )
            msg_typ = MessageType.from_str(msg_type)
            encrypt = msg_typ.protocol not in ['admin', 'microledgers']
            if not await self.__tunnel_rpc.post(message=request, encrypt=encrypt):
            raise SiriusRPCError()
            if wait_response:
            success = await future.wait(timeout=self._timeout)
            if success:
            if future.has_exception():
            future.raise_exception()
            else:
            return future.get_value()
            else:
            raise SiriusTimeoutRPC()

            async def send_message(
            self, message: Message,
            their_vk: Union[List[str], str], endpoint: str,
            my_vk: Optional[str], routing_keys: Optional[List[str]],
            coprotocol: bool=False
            ) -> Optional[Message]:
            """Send Message to other Indy compatible agent

        :param message: message
        :param their_vk: Verkey of recipients
        :param endpoint: Endpoint Address of recipient
        :param my_vk: Verkey of sender (None for anocrypt mode)
        :param routing_keys: Routing keys if it is exists
        :param coprotocol: True if message is part of co-protocol stream
            See:
             - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0003-protocols
             - https://github.com/hyperledger/aries-rfcs/tree/master/concepts/0008-message-id-and-threading
        :return: Response message if coprotocol is True
        """
            if not self._connector.is_open:
            raise SiriusConnectionClosed('Open agent connection at first')
            if isinstance(their_vk, str):
            recipient_verkeys = [their_vk]
            else:
            recipient_verkeys = their_vk
            params = {
            'message': message,
            'routing_keys': routing_keys or [],
            'recipient_verkeys': recipient_verkeys,
            'sender_verkey': my_vk
            }
            if self.__prefer_agent_side:
            params['timeout'] = self.timeout
            params['endpoint_address'] = endpoint
            ok, body = await self.remote_call(
            msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/send_message',
            params=params
            )
            else:
            wired = await self.remote_call(
            msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prepare_message_for_send',
            params=params
            )
            if endpoint.startswith('ws://') or endpoint.startswith('wss://'):
            ws = await self.__get_websocket(endpoint)
            await ws.send_bytes(wired)
            ok, body = True, b''
            else:
            ok, body = await http_send(wired, endpoint, timeout=self.timeout, connector=self.__connector)
            body = body.decode()
            if not ok:
            raise SiriusRPCError(body)
            else:
            if coprotocol:
            response = await self.read_protocol_message()
            return response
            else:
            return None

            async def send_message_batched(self, message: Message, batches: List[RoutingBatch]) -> List[Any]:
            if not self._connector.is_open:
            raise SiriusConnectionClosed('Open agent connection at first')
            params = {
            'message': message,
            'timeout': self.timeout,
            'batches': batches,
            }
            results = await self.remote_call(
            msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/send_message_batched',
            params=params
            )
            return results

            async def read_protocol_message(self) -> Message:
            response = await self.__tunnel_coprotocols.receive(timeout=self._timeout)
            return response

            async def start_protocol_with_threading(self, thid: str, ttl: int=None):
            await self.remote_call(
            msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/start_protocol',
            params={
            'thid': thid,
            'channel_address': self.__tunnel_coprotocols.address,
            'ttl': ttl
            }
            )

            async def start_protocol_with_threads(self, threads: List[str], ttl: int=None):
            await self.remote_call(
            msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/start_protocol',
            params={
            'threads': threads,
            'channel_address': self.__tunnel_coprotocols.address,
            'ttl': ttl
            }
            )

            async def start_protocol_for_p2p(self, sender_verkey: str, recipient_verkey: str, protocols: List[str], ttl: int=None):
            await self.remote_call(
            msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/start_protocol',
            params={
            'sender_verkey': sender_verkey,
            'recipient_verkey': recipient_verkey,
            'protocols': protocols,
            'channel_address': self.__tunnel_coprotocols.address,
            'ttl': ttl
            }
            )

            async def stop_protocol_with_threading(self, thid: str, off_response: bool=False):
            await self.remote_call(
            msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/stop_protocol',
            params={
            'thid': thid,
            'off_response': off_response
            },
            wait_response=not off_response
            )

            async def stop_protocol_for_p2p(
            self, sender_verkey: str, recipient_verkey: str, protocols: List[str], off_response: bool=False
            ):
            await self.remote_call(
            msg_type='did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/stop_protocol',
            params={
            'sender_verkey': sender_verkey,
            'recipient_verkey': recipient_verkey,
            'protocols': protocols,
            'off_response': off_response
            },
            wait_response=not off_response
            )

@classmethod
    def _path(cls):
            return '/rpc'

            async def _setup(self, context: Message):
            # Extract proxy info
            proxies = context.get('~proxy', [])
            channel_rpc = None
            channel_sub_protocol = None
            for proxy in proxies:
            if proxy['id'] == 'reverse':
            channel_rpc = proxy['data']['json']['address']
            elif proxy['id'] == 'sub-protocol':
            channel_sub_protocol = proxy['data']['json']['address']
            if channel_rpc is None:
            raise RuntimeError('rpc channel is empty')
            if channel_sub_protocol is None:
            raise RuntimeError('sub-protocol channel is empty')
            self.__tunnel_rpc = AddressedTunnel(
            address=channel_rpc, input_=self._connector, output_=self._connector, p2p=self._p2p
            )
            self.__tunnel_coprotocols = AddressedTunnel(
            address=channel_sub_protocol, input_=self._connector, output_=self._connector, p2p=self._p2p
            )
            # Extract active endpoints
            endpoints = context.get('~endpoints', [])
            endpoint_collection = []
            for endpoint in endpoints:
            body = endpoint['data']['json']
            address = body['address']
            frontend_key = body.get('frontend_routing_key', None)
            if frontend_key:
            for routing_key in body.get('routing_keys', []):
            is_default = routing_key['is_default']
            key = routing_key['routing_key']
            endpoint_collection.append(
            Endpoint(address=address, routing_keys=[key, frontend_key], is_default=is_default)
            )
            else:
            endpoint_collection.append(
            Endpoint(address=address, routing_keys=[], is_default=False)
            )
            if not endpoint_collection:
            raise RuntimeError('Endpoints are empty')
            self.__endpoints = endpoint_collection
            # Extract Networks
            self.__networks = context.get('~networks', [])

            async def close(self):
            await super().close()
            for ws, session in self.__websockets.values():
            await ws.close()
            await session.close()

            async def __get_websocket(self, url: str):
            tup = self.__websockets.get(url, None)
            if tup is None:
            session = aiohttp.ClientSession(timeout=aiohttp.ClientTimeout(total=self.timeout))
            ws = await session.ws_connect(url=url)
            self.__websockets[url] = (ws, session)
            else:
            ws, session = tup
            if ws.closed:
            ws = session.ws_connect(url=url)
            self.__websockets[url] = (ws, session)
            return ws
*/