package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnResponse;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.*;

public class Persistent0160 {

    private static Map<String, JSONObject> invKeyToPairwise = new HashMap<>();

    public static Optional<Pairwise> receive(Context context, Event event) {
        if (event.message() instanceof ConnRequest) {
            receiveRequest(context, (ConnRequest) event.message(), event.getRecipientVerkey(), context.getEndpointWithEmptyRoutingKeys());
        } else if (event.message() instanceof ConnResponse) {
            return Optional.ofNullable(receiveResponse(context, (ConnResponse) event.message()));
        } else {
            return receiveAck(context, event);
        }
        return Optional.empty();
    }

    public static void acceptInvitation(Context context, Invitation invitation, String myLabel) {
        Pair<String, String> myDidVk = context.getDid().createAndStoreMyDid();
        ConnRequest request = ConnRequest.builder().
                setLabel(myLabel).
                setDid(myDidVk.first).
                setVerkey(myDidVk.second).
                setEndpoint(context.getEndpointAddressWithEmptyRoutingKeys()).
                setDocUri(invitation.getDocUri()).
                build();

        String connectionKey = invitation.recipientKeys().get(0);
        context.sendMessage(request, Arrays.asList(connectionKey), invitation.endpoint(), myDidVk.second, Arrays.asList());
    }

    private static void receiveRequest(Context context, ConnRequest request, String connectionKeyBase58, Endpoint myEndpoint) {
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

        invKeyToPairwise.put(connectionKeyBase58, pairwise);
    }

    private static Pairwise receiveResponse(Context context, ConnResponse response) {

        return null;
    }

    private static Optional<Pairwise> receiveAck(Context context, Event event) {
        String senderVk = event.getSenderVerkey();
        for (Map.Entry<String, JSONObject> e : invKeyToPairwise.entrySet()) {
            JSONObject o = e.getValue();
            if (o.optJSONObject("their").optString("verkey").equals(senderVk)) {
                Pairwise.Me me = new Pairwise.Me(
                        o.optJSONObject("me").optString("did"),
                        o.optJSONObject("me").optString("verkey"),
                        o.optJSONObject("me").optJSONObject("did_doc"));

                List<String> routingKeys = new ArrayList<>();
                for (Object k : o.optJSONObject("their").optJSONArray("routing_keys"))
                    routingKeys.add((String) k);
                Pairwise.Their their = new Pairwise.Their(
                        o.optJSONObject("their").optString("did"),
                        o.optJSONObject("their").optString("label"),
                        o.optJSONObject("their").optJSONObject("endpoint").optString("address"),
                        o.optJSONObject("their").optString("verkey"),
                        routingKeys
                );
                Pairwise pairwise = new Pairwise(me, their, o);
                invKeyToPairwise.remove(e.getKey());
                return Optional.of(pairwise);
            }
        }
        return Optional.empty();
    }
}
