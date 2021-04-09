package com.sirius.sdk.hub.coprotocols;

import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

public abstract class AbstractP2PCoProtocol extends AbstractCoProtocol{

    protected AbstractP2PCoProtocol(Context context) {
        super(context);
    }

    public abstract void send(Message message) throws SiriusPendingOperation;

    public abstract Pair<Boolean, Message> sendAndWait(Message message) throws SiriusInvalidPayloadStructure, SiriusInvalidMessage;
}
