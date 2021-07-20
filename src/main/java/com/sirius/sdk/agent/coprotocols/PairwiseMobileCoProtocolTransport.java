package com.sirius.sdk.agent.coprotocols;

import com.sirius.sdk.agent.MobileAgent;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PairwiseMobileCoProtocolTransport extends AbstractCoProtocolTransport {
    MobileAgent agent;
    Pairwise pw;
    Listener listener;

    public PairwiseMobileCoProtocolTransport(MobileAgent agent, Pairwise pw) {
        this.agent = agent;
        this.pw = pw;
        this.listener = this.agent.subscribe();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        listener.unsubscribe();
    }

    @Override
    public Pair<Boolean, Message> sendAndWait(Message message) {
        send(message);
        GetOneResult r = getOne();
        if (r != null) {
            if (r.senderVerkey.equals(pw.getTheir().getVerkey())) {
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
    public void send(Message message) {
        agent.sendTo(message, pw);
    }

    @Override
    public List<Pair<Boolean, String>> sendMany(Message message, List<Pairwise> to) {
        return null;
    }
}
