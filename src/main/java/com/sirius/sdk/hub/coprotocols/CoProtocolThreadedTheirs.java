package com.sirius.sdk.hub.coprotocols;

import com.sirius.sdk.agent.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoProtocolThreadedTheirs extends AbstractCoProtocol {
    String thid;
    List<Pairwise> theirs;
    String pthid = null;
    List<String> dids = new ArrayList<>();

    public CoProtocolThreadedTheirs(Context context, String thid, List<Pairwise> theirs, String pthid, int timeToLiveSec) throws SiriusContextError {
        super(context);
        if (theirs.isEmpty()) {
            throw new SiriusContextError("theirs is empty");
        }
        this.timeToLiveSec = timeToLiveSec;
        this.thid = thid;
        this.pthid = pthid;
        this.theirs = theirs;
        for (Pairwise p : theirs) {
            dids.add(p.getTheir().getDid());
        }
    }

    public List<Pairwise> getTheirs() {
        return theirs;
    }

    public static class SendResult {
        public Pairwise pairwise;
        public Boolean success;
        public String endpoint;
    }

    /**
     * Send message to given participants
     * @param message
     * @return
     */
    public List<SendResult> send(Message message) {
        List<SendResult> res = new ArrayList<>();
        //getTransportLazy().sendMany();

        return res;
    }

    private AbstractCoProtocolTransport getTransportLazy() {
        if (transport == null) {
            if (this.pthid == null) {
                transport = context.getCurrentHub().getAgentConnectionLazy().spawn(this.thid);
            } else {
                transport = context.getCurrentHub().getAgentConnectionLazy().spawn(this.thid, this.pthid);
            }
            transport.start(timeToLiveSec);
            started = true;
        }
        return transport;
    }

    @Override
    public void close() {

    }
}
