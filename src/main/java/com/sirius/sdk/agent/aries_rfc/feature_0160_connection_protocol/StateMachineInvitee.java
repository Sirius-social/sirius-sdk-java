package com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol;

import com.sirius.sdk.agent.aries_rfc.DidDoc;
import com.sirius.sdk.agent.aries_rfc.feature_0015_acks.Ack;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.*;
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

public class StateMachineInvitee extends BaseConnectionStateMachine {
    Logger log = Logger.getLogger(StateMachineInvitee.class.getName());

    public StateMachineInvitee(Context context, Pairwise.Me me, Endpoint myEndpoint) {
        this.context = context;
        this.me = me;
        this.myEndpoint = myEndpoint;

    }

    public Pairwise createConnection(Invitation invitation, String mylabel, DidDoc didDoc) {
        // Validate invitation
        log.info("0% - Invitation validate");
        if (!invitation.validate()) {
            log.info("100% - Terminated with error");
            return null;
        } else {
            log.info("20% - Invitation validation OK");

            String docUri = invitation.getDocUri();
            // Extract Inviter connection_key
            String connectionKey = invitation.recipientKeys().get(0);
            TheirEndpoint inviterEndpoint = new TheirEndpoint(invitation.endpoint(), connectionKey);

            // Allocate transport channel between self and theirs by verkeys factor
            try {
                createCoprotocol(inviterEndpoint);
                ConnRequest request = ConnRequest.builder().
                        setLabel(mylabel).
                        setDid(this.me.getDid()).
                        setVerkey(this.me.getVerkey()).
                        setEndpoint(this.myEndpoint.getAddress()).
                        setDocUri(docUri).
                        setDidDocExtra(didDoc != null ? didDoc.getPayload() : null).
                        build();

                log.info("30% - Step-1: send connection request to Inviter");
                Pair<Boolean, Message> okMsg = coprotocol.sendAndWait(request);
                if (okMsg.first) {
                    if (okMsg.second instanceof ConnResponse) {
                        // Step 2: process connection response from Inviter
                        log.info("40% - Step-2: process connection response from Inviter");
                        ConnResponse response = (ConnResponse) okMsg.second;
                        if (!response.validate()) {
                            log.info("100% - Terminated with error");
                            return null;
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
                                coprotocol.send(ack);
                                log.info("90% - Step-4: Send ack to Inviter");
                            } else {
                                Ping ping = Ping.create("Connection established", false);
                                coprotocol.send(ping);
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
                                            put("label", request.getLabel()).
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
                            log.info("100% - Terminated with error");
                            return null;
                        }
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
        }
        log.info("100% - Terminated with error");
        return null;
    }

    public Pairwise createConnection(Invitation invitation, String mylabel) {
        return createConnection(invitation, mylabel, null);
    }

}
