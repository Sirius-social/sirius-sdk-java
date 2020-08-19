package com.sirius.sdk.agent.aries_rfc.feature_0048_trust;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;

/**Implementation of Ping part for trust_ping protocol
 *  https://github.com/hyperledger/aries-rfcs/tree/master/features/0048-trust-ping
 */
public class Ping extends AriesProtocolMessage {
    public String getComment() {
        return comment;
    }

    public Boolean getResponseRequested() {
        return responseRequested;
    }

    String comment;
    Boolean responseRequested;

    public Ping(String message) {
        super(message);
        comment =  getStringFromJSON("comment");
        responseRequested = getBooleanFromJSON("response_requested");
    }

    public Ping(String comment, Boolean responseRequested) {
        super();
        this.comment = comment;
        this.responseRequested = responseRequested;
    }

    @Override
    public String getProtocol() {
        return "trust_ping";
    }

    @Override
    public String getName() {
        return "ping";
    }
}
