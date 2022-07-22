package helpers;

import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.OfferCredentialMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Holder;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.RequestPresentationMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines.Prover;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Invitee;
import com.sirius.sdk.agent.consensus.simple.messages.ProposeTransactionsMessage;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.n_wise.NWiseParticipant;
import com.sirius.sdk.agent.n_wise.messages.FastInvitation;
import com.sirius.sdk.agent.n_wise.messages.LedgerUpdateNotify;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.indy_exceptions.DuplicateMasterSecretNameException;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.MobileContext;
import com.sirius.sdk.hub.MobileHub;
import com.sirius.sdk.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Smartphone {
    MobileHub.Config config;
    MobileContext context = null;
    boolean loop = false;
    String nickname;

    public static class NWiseMessage {
        public Message message;
        public String nWiseInternalId;
        public String senderDid;
    }

    List<NWiseMessage> receivedMessages = new ArrayList<>();
    CompletableFuture<NWiseMessage> future = null;

    public Smartphone(MobileHub.Config config, String nickname) {
        this.config = config;
        this.nickname = nickname;
    }

    public void start() {
        if (context == null) {
            context = new MobileContext(config);
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

    public String acceptInvitation(FastInvitation invitation) {
        return context.getNWiseManager().acceptInvitation(invitation, nickname);
    }

    public FastInvitation createNWiseInvitation(String internalId) {
        return context.getNWiseManager().createFastInvitation(internalId);
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

    public CompletableFuture<NWiseMessage> getMessage() {
        future = new CompletableFuture<>();
        return future;
    }

    protected void routine() {
        Listener listener = context.subscribe();
        try {
            while (loop) {
                Event event = listener.getOne().get();
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
                            if (future != null)
                                future.complete(nWiseMessage);

                        }
                    }
                } else if (event.message() instanceof LedgerUpdateNotify) {
                    context.getNWiseManager().getNotify(event.getSenderVerkey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
