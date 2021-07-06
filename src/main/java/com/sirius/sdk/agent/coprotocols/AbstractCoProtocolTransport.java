package com.sirius.sdk.agent.coprotocols;

import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCoProtocolTransport {

    public static class GetOneResult {
        public Message message;
        public String senderVerkey;
        public  String recipientVerkey;

        public GetOneResult(Message message, String senderVerkey, String recipientVerkey) {
            this.message = message;
            this.senderVerkey = senderVerkey;
            this.recipientVerkey = recipientVerkey;
        }
    }

    int timeToLiveSec = 60;
    List<String> protocols = new ArrayList<>();

    public abstract void start();
    public abstract void stop();
    public abstract Pair<Boolean, Message> sendAndWait(Message message) throws SiriusPendingOperation, SiriusInvalidPayloadStructure, SiriusInvalidMessage;
    public abstract GetOneResult getOne() throws SiriusInvalidPayloadStructure;
    public abstract void send(Message message) throws SiriusPendingOperation;
    public abstract List<Pair<Boolean, String>> sendMany(Message message, List<Pairwise> to);

    public void setTimeToLiveSec(int timeToLiveSec) {
        this.timeToLiveSec = timeToLiveSec;
    }

    public List<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<String> protocols) {
        this.protocols = protocols;
    }
}
