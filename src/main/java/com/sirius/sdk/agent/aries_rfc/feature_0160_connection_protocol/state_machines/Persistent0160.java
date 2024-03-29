package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProtocolMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnResponse;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.*;

public class Persistent0160 {

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
        JSONObject pairwise = new JSONObject().
                put("me", new JSONObject().
                        put("did", myDidVk.first).
                        put("verkey", myDidVk.second).
                        put("did_doc", request.didDoc())).
                put("their", new JSONObject()
                        .put("label", invitation.label()));
        PairwiseNonSecretStorage.write(context, connectionKey, pairwise);
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
        response.setPleaseAck(true);
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

        PairwiseNonSecretStorage.write(context, connectionKeyBase58, pairwise);
    }

    private static Pairwise receiveResponse(Context context, ConnResponse response) {
        if (!response.verifyConnection(context.getCrypto()))
            return null;
        String connectionKey = response.getMessageObj().getJSONObject("connection~sig").optString("signer");
        if (PairwiseNonSecretStorage.optValueByConnectionKey(context, connectionKey).isEmpty())
            return null;

        ConnProtocolMessage.ExtractTheirInfoRes theirInfo = null;
        try {
            theirInfo = response.extractTheirInfo();
        } catch (SiriusInvalidMessage e) {
            return null;
        }
        context.getDid().storeTheirDid(theirInfo.did, theirInfo.verkey);

        String myVk = PairwiseNonSecretStorage.optValueByConnectionKey(context, connectionKey).get().optJSONObject("me").optString("verkey");
        if (response.hasPleaseAck()) {
            Ack ack = Ack.builder().
                    setStatus(Ack.Status.OK).
                    build();
            ack.setThreadId(response.getAckMessageId());
            context.sendMessage(ack, Arrays.asList(theirInfo.verkey), theirInfo.endpoint, myVk, theirInfo.routingKeys);
        } else {
            Ping ping = Ping.builder().
                    setComment("Connection established").
                    setResponseRequested(false).
                    build();
            context.sendMessage(ping, Arrays.asList(theirInfo.verkey), theirInfo.endpoint, myVk, theirInfo.routingKeys);
        }

        JSONObject their = new JSONObject().
                put("did", theirInfo.did).
                put("verkey", theirInfo.verkey).
                put("label", PairwiseNonSecretStorage.optValueByConnectionKey(context, connectionKey).get().optJSONObject("their").optString("label")).
                put("endpoint", (new JSONObject()).
                        put("address", theirInfo.endpoint).
                        put("routing_keys", theirInfo.routingKeys)).
                put("did_doc", response.didDoc().getPayload());
        JSONObject jsonPw = PairwiseNonSecretStorage.optValueByConnectionKey(context, connectionKey).get();
        jsonPw.put("their", their);
        Pairwise pairwise = createPairwiseObject(jsonPw);
        PairwiseNonSecretStorage.remove(context, connectionKey);

        return pairwise;
    }

    private static Optional<Pairwise> receiveAck(Context context, Event event) {
        String senderVk = event.getSenderVerkey();
        Optional<String> connectionKey = PairwiseNonSecretStorage.optConnectionKeyByTheirVerkey(context, senderVk);
        if (connectionKey.isEmpty())
            return Optional.empty();

        Optional<JSONObject> jsonPw = PairwiseNonSecretStorage.optValueByConnectionKey(context, connectionKey.get());
        Pairwise pairwise = createPairwiseObject(jsonPw.get());
        PairwiseNonSecretStorage.remove(context, connectionKey.get());
        return Optional.of(pairwise);
    }

    private static Pairwise createPairwiseObject(JSONObject o) {
        Pairwise.Me me = new Pairwise.Me(
                o.optJSONObject("me").optString("did"),
                o.optJSONObject("me").optString("verkey"),
                o.optJSONObject("me").optJSONObject("did_doc"));

        List<String> routingKeys = new ArrayList<>();
        for (Object k : o.optJSONObject("their").optJSONObject("endpoint").optJSONArray("routing_keys"))
            routingKeys.add((String) k);
        Pairwise.Their their = new Pairwise.Their(
                o.optJSONObject("their").optString("did"),
                o.optJSONObject("their").optString("label"),
                o.optJSONObject("their").optJSONObject("endpoint").optString("address"),
                o.optJSONObject("their").optString("verkey"),
                routingKeys
        );
        return new Pairwise(me, their, o);
    }
}
