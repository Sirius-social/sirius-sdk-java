package helpers;

import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Persistent0160;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.ReplaySubject;

import java.util.Collections;
import java.util.Optional;

public class Agent0160 {

    boolean loop = false;
    Context context;
    String nickname;
    ReplaySubject<Pairwise> pairwises = ReplaySubject.create();

    public Agent0160(Context context, String nickname) {
        this.nickname = nickname;
        this.context = context;
    }

    public void start() {
        if (!loop) {
            loop = true;
            new Thread(() -> routine()).start();
        }
    }

    public Invitation createInvitation() {
        String connectionKeyBase58 = context.getCrypto().createKey();
        return Invitation.builder().
                setLabel("Inviter").
                setEndpoint(context.getEndpointAddressWithEmptyRoutingKeys()).
                setRecipientKeys(Collections.singletonList(connectionKeyBase58)).
                build();
    }

    public void acceptInvitation(Invitation invitation) {
        Persistent0160.acceptInvitation(context, invitation, nickname);
    }

    public Observable<Pairwise> getPairwises() {
        return pairwises;
    }

    protected void routine() {
        Listener listener = context.subscribe();
        listener.listen().blockingSubscribe(new Consumer<Event>() {
            @Override
            public void accept(Event event) {
                System.out.println(nickname + " received message " + event.getMessageObj().toString() + " from " + event.getSenderVerkey());
                Optional<Pairwise> pw = Persistent0160.receive(context, event);
                if (pw.isPresent()) {
                    context.getPairwiseList().ensureExists(pw.get());
                    pairwises.onNext(pw.get());
                }
            }
        });
    }
}
