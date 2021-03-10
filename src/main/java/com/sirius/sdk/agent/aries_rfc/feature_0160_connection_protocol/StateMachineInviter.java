package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnProblemReport;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnResponse;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.model.pairwise.TheirEndpoint;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.logging.Logger;

public class StateMachineInviter extends BaseConnectionStateMachine {
    Logger log = Logger.getLogger(StateMachineInviter.class.getName());
    String connectionKey;

    public StateMachineInviter(Context context, Pairwise.Me me, String connectionKey, Endpoint myEndpoint) {
        this.context = context;
        this.me = me;
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
        ConnRequest.ExtractTheirInfoRes theirInfo;
        try {
            theirInfo = request.extractTheirInfo();
        } catch (SiriusInvalidMessage siriusInvalidMessage) {
            siriusInvalidMessage.printStackTrace();
            log.info("100% - Terminated with error");
            return null;
        }

        TheirEndpoint inviteeEndpoint = new TheirEndpoint(theirInfo.endpoint,
                theirInfo.verkey, theirInfo.routingKeys);

        // Allocate transport channel between self and theirs by verkeys factor
        try {
            createCoprotocol(inviteeEndpoint);
            // Step 2: build connection response
            ConnResponse response = ConnResponse.builder().
                    setDid(this.me.getDid()).
                    setVerkey(this.me.getVerkey()).
                    setEndpoint(this.myEndpoint.getAddress()).
                    setDocUri(docUri).
                    setDidDocExtra(didDoc).
                    build();
            if (request.hasPleaseAck()) {
                response.setThreadId(request.getAckMessageId());
            }
            DidDoc myDidDoc = response.didDoc();
            response.signConnection(context.getCrypto(), this.connectionKey);

            log.info("80% - Step-2: Connection response");
            Pair<Boolean, Message> okMsg = coprotocol.sendAndWait(response);
            if (okMsg.first) {
                if (okMsg.second instanceof Ack || okMsg.second instanceof Ping) {
                    // Step 3: store their did
                    log.info("90% - Step-3: Ack received, store their DID");
                    context.getDid().storeTheirDid(theirInfo.did, theirInfo.verkey);
                    // Step 4: create pairwise
                    Pairwise.Their their = new Pairwise.Their(theirInfo.did, request.getLabel(), theirInfo.endpoint, theirInfo.verkey, theirInfo.routingKeys);
                    JSONObject theirDidDoc = request.didDoc().getPayload();
                    JSONObject metadata = (new JSONObject()).
                            put("me", (new JSONObject()).
                                    put("did", this.me.getDid()).
                                    put("verkey", this.me.getVerkey()).
                                    put("did_doc", myDidDoc.getPayload())).
                            put("their", (new JSONObject().
                                    put("did", theirInfo.did).
                                    put("verkey", theirInfo.verkey).
                                    put("label", request.getLabel()).
                                    put("endpoint", (new JSONObject()).
                                            put("address", theirInfo.endpoint).
                                            put("routing_keys", theirInfo.routingKeys)).
                                    put("did_doc", theirDidDoc)));
                    Pairwise pairwise = new Pairwise(this.me, their, metadata);
                    pairwise.getMe().setDidDoc(myDidDoc.getPayload());
                    pairwise.getTheir().setDidDoc(theirDidDoc);
                    log.info("100% - Pairwise established");
                    return pairwise;
                } else if (okMsg.second instanceof ConnProblemReport) {
                    log.info("100% - Terminated with error");
                    return null;
                }
            }
        } catch (SiriusPendingOperation | SiriusInvalidPayloadStructure | SiriusInvalidMessage siriusPendingOperation) {
            siriusPendingOperation.printStackTrace();
            log.info("100% - Terminated with error");
            return null;
        } finally {
            releaseCoprotocol();
        }
        return null;
    }

    public Pairwise createConnection(ConnRequest request) {
        return createConnection(request, null);
    }
}
