package helpers;

import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.n_wise.NWiseParticipant;
import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.agent.n_wise.messages.LedgerUpdateNotify;
import com.sirius.sdk.hub.Context;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.ReplaySubject;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNWiseClient {

    public static class NWiseMessage {
        public Message message;
        public String nWiseInternalId;
        public String senderDid;
    }

    Context context = null;
    boolean loop = false;
    String nickname;
    List<NWiseMessage> receivedMessages = new ArrayList<>();
    ReplaySubject<Event> observable = ReplaySubject.create();

    public void start() {
        if (context == null) {
            loop = true;
            new Thread(() -> routine()).start();
        }
    }

    public void stop() {
        if (context != null) {
            loop = false;
            context.close();
        }
    }

    public String createNWise(String nWiseName) {
        return context.getNWiseManager().create(nWiseName, nickname);
    }

    public String acceptInvitation(Invitation invitation) {
        return context.getNWiseManager().acceptInvitation(invitation, nickname);
    }

    public Invitation createNWiseInvitation(String internalId) {
        return context.getNWiseManager().createInvitation(internalId);
    }

    public boolean updateNWise(String internalId) {
        return context.getNWiseManager().update(internalId);
    }

    public List<NWiseParticipant> getNWiseParticipants(String internalId) {
        return context.getNWiseManager().getParticipants(internalId);
    }

    public boolean sendNWiseMessage(String internalId, Message message) {
        return context.getNWiseManager().send(internalId, message);
    }

    public List<NWiseMessage> getReceivedMessages() {
        return receivedMessages;
    }

    public Observable<Event> getEvents() {
        return observable;
    }

    protected void routine() {
        Listener listener = context.subscribe();
        try {
            listener.listen().blockingSubscribe(new Consumer<Event>() {
                @Override
                public void accept(Event event) {
                    System.out.println(nickname + "    " + event.message());
                    if (event.message() instanceof Message) {
                        String nWiseId = context.getNWiseManager().resolveNWiseId(event.getSenderVerkey());
                        if (nWiseId != null) {
                            Message message = (Message) event.message();
                            NWiseParticipant sender = context.getNWiseManager().resolveParticipant(event.getSenderVerkey());
                            if (sender != null) {
                                System.out.println("Received new message from " + sender.nickname + " : " + message.getContent());
                                NWiseMessage nWiseMessage = new NWiseMessage();
                                nWiseMessage.message = message;
                                nWiseMessage.nWiseInternalId = context.getNWiseManager().resolveNWiseId(event.getSenderVerkey());
                                nWiseMessage.senderDid = sender.did;
                                receivedMessages.add(nWiseMessage);

                            }
                        }
                    } else if (event.message() instanceof LedgerUpdateNotify) {
                        context.getNWiseManager().getNotify(event.getSenderVerkey());
                    }
                    observable.onNext(event);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
