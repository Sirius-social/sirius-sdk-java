package com.sirius.sdk.agent.connections;

import com.neovisionaries.ws.client.WebSocket;
import com.sirius.sdk.agent.RemoteParams;
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

import java.util.*;

/**
 * RPC service.
 * <p>
 * Proactive form of Smart-Contract design
 */
public class AgentRPC extends WebSocketAgentConnection {

    List<Endpoint> endpoints;
    List<String> networks;
    Map<String, WebSocket> websockets;
    boolean preferAgentSide;
    AddressedTunnel tunnelRpc;
    AddressedTunnel tunnelCoprotocols;

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
        tunnelCoprotocols = null;
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
    public Object remoteCall(String msgType, RemoteParams params, boolean waitResponse)
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
        boolean isEncryptes = !Arrays.asList("admin", "microledgers", "microledgers-batched").contains(msgTyp.getProtocol());

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
        tunnelCoprotocols = new AddressedTunnel(channel_sub_protocol, connector, connector, p2p);
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
                               String myVk, List<String> routingKeys, boolean coprotocol) throws SiriusConnectionClosed, SiriusRPCError, SiriusInvalidPayloadStructure {
        if (!connector.isOpen()) {
            throw new SiriusConnectionClosed("Open agent connection at first");
        }
        RemoteParams.RemoteParamsBuilder paramsBuilder = RemoteParams.RemoteParamsBuilder.create()
                .add("message", message);
        if(routingKeys ==null) {
            routingKeys = new ArrayList<>();
        }
        paramsBuilder.add("routing_keys", routingKeys)
                .add("recipient_verkeys", their_vk)
                .add("sender_verkey", myVk);

        Object response = null;
        if (preferAgentSide) {
            paramsBuilder.add("timeout", timeout);
            paramsBuilder.add("endpoint_address", endpoint);
            try {
               response = remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/send_message", paramsBuilder.build());

            } catch (Exception siriusRPCError) {
                siriusRPCError.printStackTrace();
            }
        } else {
            try {
                Object wired = remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/prepare_message_for_send", paramsBuilder.build());
                if (endpoint.startsWith("ws://") || endpoint.startsWith("wss://")) {

                }
            } catch (Exception siriusRPCError) {
                siriusRPCError.printStackTrace();
            }
        }

        boolean ok = (boolean) ((Pair) response).first;
        String body = (String) ((Pair) response).second;

        if (!ok) {
            throw new SiriusRPCError(body);
        } else {
            if (coprotocol) {
                return readProtocolMessage();
            }
        }

        return null;
    }

    public List<Pair<Boolean, String>> sendMessageBatched(Message message, List<RoutingBatch> batches) throws SiriusConnectionClosed {
        if (!connector.isOpen()) {
            throw new SiriusConnectionClosed("Open agent connection at first");
        }

        RemoteParams params = RemoteParams.RemoteParamsBuilder.create().
                add("message", message).
                add("timeout", this.timeout).
                add("batches", batches).
                build();

        try {
            Object response = remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/send_message_batched", params);
            JSONArray jsonArr = (JSONArray) response;
            List<Pair<Boolean, String>> res = new ArrayList<>();
            for (Object o : jsonArr) {
                JSONArray internalArr = (JSONArray) o;
                res.add(new Pair<>(internalArr.getBoolean(0), internalArr.get(1).toString()));
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void startProtocolWithThreads(List<String> threads, int timeToLiveSec) {
        try {
            this.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/start_protocol",
                    RemoteParams.RemoteParamsBuilder.create()
                            .add("threads", threads)
                            .add("channel_address", this.tunnelCoprotocols.getAddress())
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

    public void startProtocolWithThreading(String thid, int timeToLiveSec) {
        try {
            this.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/start_protocol",
                    RemoteParams.RemoteParamsBuilder.create()
                            .add("thid", thid)
                            .add("channel_address", this.tunnelCoprotocols.getAddress())
                            .add("ttl", timeToLiveSec).build());
        } catch (Exception siriusRPCError) {
            siriusRPCError.printStackTrace();
        }
    }

    public void stopProtocolWithThreading(String thid, boolean offResponse) {
        try {
            this.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/stop_protocol",
                    RemoteParams.RemoteParamsBuilder.create()
                            .add("thid", thid)
                            .add("off_response", offResponse).build(),
                    !offResponse);
        } catch (Exception siriusRPCError) {
            siriusRPCError.printStackTrace();
        }
    }

    public void startProtocolForP2P(String senderVerkey, String recipientVerkey, List<String> protocols, int timeToLiveSec) {
        try {
            this.remoteCall("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/start_protocol",
                    RemoteParams.RemoteParamsBuilder.create()
                            .add("sender_verkey", senderVerkey)
                            .add("recipient_verkey", recipientVerkey)
                            .add("protocols", protocols)
                            .add("channel_address", this.tunnelCoprotocols.getAddress())
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

    public Message readProtocolMessage() throws SiriusInvalidPayloadStructure {
        return this.tunnelCoprotocols.receive(timeout);
    }

}
