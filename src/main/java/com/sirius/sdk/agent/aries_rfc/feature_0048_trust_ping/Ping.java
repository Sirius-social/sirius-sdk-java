package com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping;

import com.sirius.sdk.agent.aries_rfc.AriesProtocolMessage;
import com.sirius.sdk.messaging.Message;
import org.json.JSONObject;

/**Implementation of Ping part for trust_ping protocol
 *  https://github.com/hyperledger/aries-rfcs/tree/master/features/0048-trust-ping
 */
public class Ping extends AriesProtocolMessage {
    public static final String PROTOCOL = "trust_ping";

    static {
        Message.registerMessageClass(Ping.class, Ping.PROTOCOL, "ping");
    }

    public String getComment() {
        return getStringFromJSON("comment");
    }

    public Boolean getResponseRequested() {
        return getBooleanFromJSON("response_requested");
    }

    public Ping(String message) {
        super(message);
    }

    static public Ping create(String comment, Boolean responseRequested) {
        JSONObject pingObject = new JSONObject();
        pingObject.put("@id", generateId());
        pingObject.put("@type", ARIES_DOC_URI + "trust_ping/1.0/ping");
        pingObject.put("comment", comment);
        pingObject.put("response_requested", responseRequested);
        return new Ping(pingObject.toString());
    }
}
