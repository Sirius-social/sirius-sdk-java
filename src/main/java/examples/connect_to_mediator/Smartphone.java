package examples.connect_to_mediator;

import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.OfferCredentialMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Holder;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.RequestPresentationMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Invitee;
import com.sirius.sdk.agent.consensus.simple.messages.ProposeTransactionsMessage;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.MobileContext;
import com.sirius.sdk.hub.MobileHub;
import com.sirius.sdk.utils.Pair;

import java.util.concurrent.ExecutionException;

public class Smartphone {
    MobileHub.Config config;
    MobileContext context = null;
    Pairwise.Me me = null;
    boolean loop = false;

    public Smartphone(MobileHub.Config config) {
        this.config = config;
    }

    public void start() {
        if (context == null) {
            context = new MobileContext(config);
            Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
            me = new Pairwise.Me(didVk.first, didVk.second);
            //context.addMediatorKey(me.getVerkey());
            loop = true;
            new Thread(() -> routine()).start();
        }
    }

    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    public void acceptInvitation(Invitation invitation) {
        Invitee invitee = new Invitee(context, me, context.getEndpointWithEmptyRoutingKeys());
        Pairwise pw = invitee.createConnection(invitation, "Edge agent");
        if (pw != null) {
            context.getPairwiseList().ensureExists(pw);
        }
    }

    protected void routine() {
        Listener listener = context.subscribe();
        try {
            while (loop) {
                Event event = listener.getOne().get();
                if (event.message() instanceof OfferCredentialMessage && event.getPairwise() != null) {
                    OfferCredentialMessage offer = (OfferCredentialMessage) event.message();
                    Holder holder = new Holder(context, event.getPairwise());
                    Pair<Boolean, String> res = holder.accept(offer, "prover_master_secret_name", "", "en");
                } else if (event.message() instanceof RequestPresentationMessage && event.getPairwise() != null) {
                    RequestPresentationMessage request = (RequestPresentationMessage) event.message();
                } else if (event.message() instanceof Message && event.getPairwise() != null) {
                    Message message = (Message) event.message();
                    System.out.println("Received new message: " + message.getContent());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
