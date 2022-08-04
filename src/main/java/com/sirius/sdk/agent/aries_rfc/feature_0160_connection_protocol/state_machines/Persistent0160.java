package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnResponse;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Persistent0160 {

    private static Map<String, JSONObject> invKeyToPairwise = new HashMap<>();

    public static boolean receive(Context context, Event event) {
        if (event.message() instanceof ConnRequest) {
            receive(context, (ConnRequest) event.message(), event.getRecipientVerkey(), context.getEndpointWithEmptyRoutingKeys());
            return true;
        } else if (event.message() instanceof ConnResponse) {
            receive(context, (ConnResponse) event.message());
        }
        return false;
    }

    private static void receive(Context context, ConnRequest request, String connectionKeyBase58, Endpoint myEndpoint) {
        ConnRequest.ExtractTheirInfoRes theirInfo;
        try {
            theirInfo = request.extractTheirInfo();
        } catch (SiriusInvalidMessage e) {
            e.printStackTrace();
            return;
        }

        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();

        ConnResponse response = ConnResponse.builder().
                setDid(didVk.first).
                setVerkey(didVk.second).
                setEndpoint(myEndpoint.getAddress()).
                setDocUri(request.getDocUri()).
                build();
        if (request.hasPleaseAck()) {
            response.setThreadId(request.getAckMessageId());
        } else {
            response.setThreadId(request.getId());
        }
        DidDoc myDidDoc = response.didDoc();
        JSONObject theirDidDoc = request.didDoc().getPayload();
        response.signConnection(context.getCrypto(), connectionKeyBase58);
        context.sendMessage(response, Arrays.asList(theirInfo.verkey), theirInfo.endpoint, didVk.second, theirInfo.routingKeys);

        JSONObject pairwise = (new JSONObject()).
                put("me", (new JSONObject()).
                        put("did", didVk.first).
                        put("verkey", didVk.second).
                        put("did_doc", myDidDoc.getPayload())).
                put("their", (new JSONObject().
                        put("did", theirInfo.did).
                        put("verkey", theirInfo.verkey).
                        put("label", request.getLabel()).
                        put("endpoint", (new JSONObject()).
                                put("address", theirInfo.endpoint).
                                put("routing_keys", theirInfo.routingKeys)).
                        put("did_doc", theirDidDoc)));
    }

    private static void receive(Context context, ConnResponse response) {

    }
}
