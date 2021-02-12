package com.sirius.sdk.agent.aries_rfc;

import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.messaging.Message;

public abstract class AriesProtocolMessage extends Message {

    public static final String ARIES_DOC_URI = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/";
    public static final String THREAD_DECORATOR = "~thread";

    public AriesProtocolMessage(String message) {
        super(message);
        Message.registerMessageClass(this.getClass(), getProtocol(), getName());
    }

    public AriesProtocolMessage() {
        super("{}");
        Message.registerMessageClass(this.getClass(), getProtocol(), getName());
    }

    public abstract String getProtocol();

    public abstract String getName();


}
