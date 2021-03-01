package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol;

import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.model.pairwise.TheirEndpoint;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.hub.Context;
import org.json.JSONObject;

import java.util.logging.Logger;

public class StateMachineInviter extends BaseConnectionStateMachine {
    Logger log = Logger.getLogger(StateMachineInvitee.class.getName());
    String connectionKey;

    public StateMachineInviter(Context context, String connectionKey, Endpoint myEndpoint) {
        this.context = context;
        this.connectionKey = connectionKey;
        this.myEndpoint = myEndpoint;
    }

    public Pairwise createConnection(ConnRequest request, JSONObject didDoc) {
        // Validate request
        log.info("0% - Validate request");
        if (!request.validate()) {
            log.info("100% - Terminated with error");
            return null;
        }

        log.info("20% - Request validation OK");

        // Step 1: Extract their info from connection request
        log.info("40% - Step-1: Extract their info from connection request");
        String docUri = request.getDocUri();
        ConnRequest.ExtractTheirInfoRes extractTheirInfoRes;
        try {
            extractTheirInfoRes = request.extractTheirInfo();
        } catch (SiriusInvalidMessage siriusInvalidMessage) {
            siriusInvalidMessage.printStackTrace();
            log.info("100% - Terminated with error");
            return null;
        }

        TheirEndpoint inviteeEndpoint = new TheirEndpoint(extractTheirInfoRes.endpoint,
                extractTheirInfoRes.verkey, extractTheirInfoRes.routingKeys);

        // Allocate transport channel between self and theirs by verkeys factor


        return null;
    }
}
