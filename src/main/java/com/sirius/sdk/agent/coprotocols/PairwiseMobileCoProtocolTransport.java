package com.sirius.sdk.agent.coprotocols;

import com.sirius.sdk.agent.MobileAgent;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

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
            Iterable<Event> ie = listener.listen().filter(event1 -> {
                return event1.getRecipientVerkey().equals(pw.getMe().getVerkey()) && event1.getSenderVerkey().equals(pw.getTheir().getVerkey());
            }).timeout(timeToLiveSec, TimeUnit.SECONDS).blockingNext();
            Event event = ie.iterator().next();
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
