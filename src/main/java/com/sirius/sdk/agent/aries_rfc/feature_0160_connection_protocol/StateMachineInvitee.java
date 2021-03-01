package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol;

import com.sirius.sdk.agent.AbstractStateMachine;
import com.sirius.sdk.agent.TransportLayer;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Holder;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;
import org.json.JSONObject;

import java.util.List;
import java.util.logging.Logger;

public class StateMachineInvitee extends BaseConnectionStateMachine {
    Logger log = Logger.getLogger(Holder.class.getName());
    String connectionKey;

    public StateMachineInvitee(Context context, String connectionKey, Endpoint myEndpoint) {
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
        //request


        return null;
    }


}
