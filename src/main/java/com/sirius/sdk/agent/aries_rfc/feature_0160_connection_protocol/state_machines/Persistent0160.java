package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnResponse;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.hub.Context;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Persistent0160 {

    private static Map<String, JSONObject> invKeyToMyPeerDid = new HashMap<>();

    private static void receive(Context context, ConnRequest request) {
        String docUri = request.getDocUri();
        ConnRequest.ExtractTheirInfoRes theirInfo;
        try {
            theirInfo = request.extractTheirInfo();
        } catch (SiriusInvalidMessage e) {
            e.printStackTrace();
            return;
        }

        TheirEndpoint inviteeEndpoint = new TheirEndpoint(theirInfo.endpoint,
                theirInfo.verkey, theirInfo.routingKeys);

        ConnResponse response = ConnResponse.builder().
//                setDid(this.me.getDid()).
//                setVerkey(this.me.getVerkey()).
//                setEndpoint(this.myEndpoint.getAddress()).
//                setDocUri(docUri).
//                setDidDocExtra(didDoc).
                build();
        if (request.hasPleaseAck()) {
            response.setThreadId(request.getAckMessageId());
        } else {
            response.setThreadId(request.getId());
        }
        DidDoc myDidDoc = response.didDoc();
        //response.signConnection(context.getCrypto(), this.connectionKey);
    }

    private static void receive(Context context, ConnResponse response) {

    }
}
