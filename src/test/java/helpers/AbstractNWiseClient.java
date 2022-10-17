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
import io.reactivex.rxjava3.schedulers.Schedulers;
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
    String nickname;
    List<NWiseMessage> receivedMessages = new ArrayList<>();
    ReplaySubject<Event> observable = ReplaySubject.create();

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

    public NWiseParticipant getMe(String internalId) {
        return context.getNWiseManager().getMe(internalId, context);
    }

    public boolean leave(String internalId) {
        return context.getNWiseManager().leave(internalId, context);
    }

    protected void routine() {
        Listener listener = context.subscribe();
        listener.listen().observeOn(Schedulers.newThread()).subscribe(new Consumer<Event>() {
            @Override
            public void accept(Event event) {
                String nWiseId = context.getNWiseManager().resolveNWiseId(event.getSenderVerkey());
                NWiseParticipant sender = context.getNWiseManager().resolveParticipant(event.getSenderVerkey());
                String senderNickname = sender != null ? sender.nickname : "Unknown";
                System.out.println(nickname + " received new message from " + senderNickname + " : " + event.message().getMessageObj());
                if (event.message() instanceof Message) {
                    if (nWiseId != null && sender != null) {
                        Message message = (Message) event.message();
                        NWiseMessage nWiseMessage = new NWiseMessage();
                        nWiseMessage.message = message;
                        nWiseMessage.nWiseInternalId = context.getNWiseManager().resolveNWiseId(event.getSenderVerkey());
                        nWiseMessage.senderDid = sender.did;
                        receivedMessages.add(nWiseMessage);
                    }
                } else if (event.message() instanceof LedgerUpdateNotify) {
                    context.getNWiseManager().getNotify(event.getSenderVerkey());
                }
                observable.onNext(event);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Throwable {
                throwable.printStackTrace();
            }
        });
    }
}
