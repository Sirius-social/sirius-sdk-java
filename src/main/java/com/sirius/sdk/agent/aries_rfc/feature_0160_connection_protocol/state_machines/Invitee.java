package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.*;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.errors.StateMachineTerminatedWithError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusValidationError;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.coprotocols.AbstractP2PCoProtocol;
import com.sirius.sdk.hub.coprotocols.CoProtocolP2PAnon;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;
import java.util.logging.Logger;

public class Invitee extends BaseConnectionStateMachine {
    Logger log = Logger.getLogger(Invitee.class.getName());

    public Invitee(Context context, Pairwise.Me me, Endpoint myEndpoint) {
        this.context = context;
        this.me = me;
        this.myEndpoint = myEndpoint;
    }

    public Pairwise createConnection(Invitation invitation, String myLabel, DidDoc didDoc) {
        // Validate invitation
        log.info("0% - Invitation validate");
        try {
            invitation.validate();
        } catch (SiriusValidationError e) {
            e.printStackTrace();
            log.info("100% - Terminated with error");
            return null;
        }
        log.info("20% - Invitation validation OK");


        String docUri = invitation.getDocUri();
        // Extract Inviter connection_key
        String connectionKey = invitation.recipientKeys().get(0);
        TheirEndpoint inviterEndpoint = new TheirEndpoint(invitation.endpoint(), connectionKey);

        // Allocate transport channel between self and theirs by verkeys factor
        try (AbstractP2PCoProtocol cp = new CoProtocolP2PAnon(context, me.getVerkey(), inviterEndpoint, protocols(), timeToLiveSec)) {
            try {
                ConnRequest request = ConnRequest.builder().
                        setLabel(myLabel).
                        setDid(this.me.getDid()).
                        setVerkey(this.me.getVerkey()).
                        setEndpoint(this.myEndpoint.getAddress()).
                        setDocUri(docUri).
                        setDidDocExtra(didDoc != null ? didDoc.getPayload() : null).
                        build();

                log.info("30% - Step-1: send connection request to Inviter");
                Pair<Boolean, Message> okMsg = cp.sendAndWait(request);
                if (okMsg.first) {
                    if (okMsg.second instanceof ConnResponse) {
                        // Step 2: process connection response from Inviter
                        log.info("40% - Step-2: process connection response from Inviter");
                        ConnResponse response = (ConnResponse) okMsg.second;
                        try {
                            response.validate();
                        } catch (SiriusValidationError e) {
                            throw new StateMachineTerminatedWithError(RESPONSE_NOT_ACCEPTED, e.getMessage());
                        }
                        boolean success = response.verifyConnection(context.getCrypto());
                        if (success && response.getMessageObj().getJSONObject("connection~sig").optString("signer").equals(connectionKey)) {
                            // Step 3: extract Inviter info and store did
                            log.info("70% - Step-3: extract Inviter info and store DID");
                            ConnProtocolMessage.ExtractTheirInfoRes theirInfo = response.extractTheirInfo();
                            context.getDid().storeTheirDid(theirInfo.did, theirInfo.verkey);

                            // Step 4: Send ack to Inviter
                            if (response.hasPleaseAck()) {
                                Ack ack = Ack.builder().
                                        setStatus(Ack.Status.OK).
                                        build();
                                ack.setThreadId(response.getAckMessageId());
                                cp.send(ack);
                                log.info("90% - Step-4: Send ack to Inviter");
                            } else {
                                Ping ping = Ping.builder().
                                        setComment("Connection established").
                                        setResponseRequested(false).
                                        build();
                                cp.send(ping);
                                log.info("90% - Step-4: Send ping to Inviter");
                            }

                            // Step 5: Make Pairwise instance
                            Pairwise.Their their = new Pairwise.Their(theirInfo.did,
                                    invitation.label(), theirInfo.endpoint, theirInfo.verkey, theirInfo.routingKeys);
                            JSONObject myDidDoc = request.didDoc().getPayload();
                            JSONObject theirDidDoc = response.didDoc().getPayload();

                            JSONObject metadata = (new JSONObject()).
                                    put("me", (new JSONObject()).
                                            put("did", this.me.getDid()).
                                            put("verkey", this.me.getVerkey()).
                                            put("did_doc", myDidDoc).
                                            put("their", (new JSONObject().
                                                    put("did", theirInfo.did).
                                                    put("verkey", theirInfo.verkey).
                                                    put("label", their.getLabel()).
                                                    put("endpoint", (new JSONObject()).
                                                            put("address", theirInfo.endpoint).
                                                            put("routing_keys", theirInfo.routingKeys)).
                                                    put("did_doc", theirDidDoc))));

                            Pairwise pairwise = new Pairwise(this.me, their, metadata);
                            pairwise.getMe().setDidDoc(myDidDoc);
                            pairwise.getTheir().setDidDoc(theirDidDoc);
                            log.info("100% - Pairwise established");
                            return pairwise;
                        } else {
                            throw new StateMachineTerminatedWithError(RESPONSE_NOT_ACCEPTED,
                                    "Invalid connection response signature for connection_key: " + connectionKey);
                        }
                    } else if (okMsg.second instanceof ConnProblemReport) {
                        this.problemReport = (ConnProblemReport) okMsg.second;
                        log.info("100% - Terminated with error. " + this.problemReport.getMessageObj());
                        return null;
                    } else {
                        throw new StateMachineTerminatedWithError(RESPONSE_NOT_ACCEPTED,
                                "Unexpected message from Inviter: " + okMsg.second.getType());
                    }
                } else {
                    throw new StateMachineTerminatedWithError(RESPONSE_PROCESSING_ERROR, "Response awaiting was terminated by timeout", false);
                }
            } catch (StateMachineTerminatedWithError e) {
                this.problemReport = ConnProblemReport.builder().
                        setProblemCode(e.getProblemCode()).
                        setExplain(e.getExplain()).
                        build();
                if (e.isNotify()) {
                    cp.send(problemReport);
                    log.info("100% - Terminated with error. " + e.getMessage());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public Pairwise createConnection(Invitation invitation, String myLabel) {
        return createConnection(invitation, myLabel, null);
    }

}
