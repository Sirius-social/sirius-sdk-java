package com.sirius.sdk.agent.coprotocols;

import com.sirius.sdk.agent.MobileAgent;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.pairwise.TheirEndpoint;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidMessage;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidPayloadStructure;
import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TheirEndpointMobileCoProtocolTransport extends AbstractCoProtocolTransport {
    MobileAgent agent;
    String myVerkey;
    TheirEndpoint endpoint;
    Listener listener;

    public TheirEndpointMobileCoProtocolTransport(MobileAgent agent, String myVerkey, TheirEndpoint endpoint) {
        this.agent = agent;
        this.myVerkey = myVerkey;
        this.endpoint = endpoint;
        listener = agent.subscribe();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Pair<Boolean, Message> sendAndWait(Message message) {
        send(message);
        GetOneResult r = getOne();
        if (r != null) {
            if (r.senderVerkey.equals(endpoint.getVerkey())) {
                return new Pair<>(true, r.message);
            }
        }
        return new Pair<>(false, null);
    }

    @Override
    public GetOneResult getOne() {
        try {
            Event event = listener.getOne().get(timeToLiveSec, TimeUnit.SECONDS);
            return new GetOneResult(event.message(), event.getSenderVerkey(), event.getRecipientVerkey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void send(Message message) throws SiriusPendingOperation {
        this.agent.sendMessage(message, Arrays.asList(endpoint.getVerkey()), endpoint.getEndpointAddress(), myVerkey, new ArrayList<>());
    }

    @Override
    public List<Pair<Boolean, String>> sendMany(Message message, List<Pairwise> to) {
        return null;
    }
}
