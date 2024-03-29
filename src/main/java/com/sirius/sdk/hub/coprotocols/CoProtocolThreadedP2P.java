package com.sirius.sdk.hub.coprotocols;

import com.sirius.sdk.agent.coprotocols.AbstractCloudCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

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

    public CoProtocolThreadedP2P(Context context, String thid, Pairwise to) {
        super(context);
        this.thid = thid;
        this.to = to;
    }

    public CoProtocolThreadedP2P(Context context, String thid, Pairwise to, int timeToLiveSec) {
        super(context);
        this.thid = thid;
        this.to = to;
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
        getTransportLazy().send(message);
    }

    @Override
    public Pair<Boolean, Message> sendAndWait(Message message) throws SiriusInvalidPayloadStructure, SiriusInvalidMessage {
        return getTransportLazy().sendAndWait(message);
    }

    private AbstractCoProtocolTransport getTransportLazy() {
        if (transport == null) {
            if (this.pthid == null) {
                transport = context.getCurrentHub().getAgentConnectionLazy().spawn(this.thid, this.to);
            } else {
                transport = context.getCurrentHub().getAgentConnectionLazy().spawn(this.thid, this.to, this.pthid);
            }
            transport.setTimeToLiveSec(timeToLiveSec);
            transport.start();
            started = true;
        }
        return transport;
    }
}
