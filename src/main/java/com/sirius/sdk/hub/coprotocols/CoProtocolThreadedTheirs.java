package com.sirius.sdk.hub.coprotocols;

import com.sirius.sdk.agent.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusContextError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

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
        public String body;

        public SendResult(Pairwise pairwise, Boolean success, String body) {
            this.pairwise = pairwise;
            this.success = success;
            this.body = body;
        }
    }

    /**
     * Send message to given participants
     * @param message
     * @return
     */
    public List<SendResult> send(Message message) {
        List<SendResult> res = new ArrayList<>();
        try {
            List<Object> responces = getTransportLazy().sendMany(message, this.theirs);
            for (int i = 0; i < responces.size(); i++) {
                Pair<Boolean, String> responce = (Pair<Boolean, String>) responces.get(i);
                res.add(new SendResult(this.theirs.get(i), responce.first, responce.second));
            }
        } catch (SiriusPendingOperation siriusPendingOperation) {
            siriusPendingOperation.printStackTrace();
        }
        return res;
    }

    public static class GetOneResult {
        public Pairwise pairwise = null;
        public Message message = null;

        public GetOneResult(Pairwise pairwise, Message message) {
            this.pairwise = pairwise;
            this.message = message;
        }
    }

    /**
     * Read event from any of participants at given timeout
     * @return
     */
    public GetOneResult getOne() {
        try {
            AbstractCoProtocolTransport.GetOneResult getOneResult = getTransportLazy().getOne();
            Pairwise p2p = loadP2PFromVerkey(getOneResult.senderVerkey);
            return new GetOneResult(p2p, getOneResult.message);
        } catch (SiriusInvalidPayloadStructure siriusInvalidPayloadStructure) {
            siriusInvalidPayloadStructure.printStackTrace();
        }
        return new GetOneResult(null, null);
    }

    public static class SendAndWaitResult {
        public Pairwise pairwise;
        public Boolean success;
        public Message message;

        public SendAndWaitResult(Pairwise pairwise, Boolean success, Message message) {
            this.pairwise = pairwise;
            this.success = success;
            this.message = message;
        }
    }

    /**
     * Switch state while participants at given timeout give responses
     * @return
     */
    public List<SendAndWaitResult> sendAndWait(Message message) {
        List<SendResult> statuses = send(message);
        int resSize = 0;
        for (SendResult sr : statuses) {
            resSize++;
        }
        int accum = 0;
        List<SendAndWaitResult> results = new ArrayList<>();
        while (accum < results.size()) {
            GetOneResult getOneResult = this.getOne();
            if (getOneResult.pairwise == null)
                break;
            if (dids.contains(getOneResult.pairwise.getTheir().getDid())) {
                results.add(new SendAndWaitResult(getOneResult.pairwise, true, getOneResult.message));
            }
        }

        return null;
    }

    private Pairwise loadP2PFromVerkey(String verkey) {
        for (Pairwise p2p : this.theirs) {
            if (p2p.getTheir().getVerkey().equals(verkey))
                return p2p;
        }
        return null;
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
}
