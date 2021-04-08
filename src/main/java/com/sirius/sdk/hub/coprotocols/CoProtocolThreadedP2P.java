package com.sirius.sdk.hub.coprotocols;

import com.sirius.sdk.agent.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

import java.io.IOException;

public class CoProtocolThreadedP2P extends AbstractP2PCoProtocol {
    String thid;
    Pairwise to;
    String pthid = null;

    public CoProtocolThreadedP2P(Context context, String thid, Pairwise to, String pthid, int timeToLiveSec) {
        super(context);
        this.thid = thid;
        this.to = to;
        this.pthid = pthid;
        this.timeToLiveSec = timeToLiveSec;
    }

    public String getThid() {
        return thid;
    }

    public String getPthid() {
        return pthid;
    }

    @Override
    public void send(Message message) throws SiriusPendingOperation {

    }

    @Override
    public Pair<Boolean, Message> sendAndWait(Message message) throws SiriusInvalidPayloadStructure, SiriusInvalidMessage, SiriusPendingOperation {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    private AbstractCoProtocolTransport getTransportLazy() {
        if (transport == null) {
            if (this.pthid == null) {
                transport = context.getCurrentHub().getAgentConnectionLazy().spawn(this.thid, this.to);
            } else {
                transport = context.getCurrentHub().getAgentConnectionLazy().spawn(this.thid, this.to, this.pthid);
            }
            transport.start(timeToLiveSec);
            started = true;
        }
        return transport;
    }
}
