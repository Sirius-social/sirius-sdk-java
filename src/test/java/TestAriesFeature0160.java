import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Invitee;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class TestAriesFeature0160 {

    ConfTest confTest;

    public static void runInviter(Context context, String expectedConnectionKey,
                                  Pairwise.Me me) {
        try {
            Endpoint myEndpoint = context.getEndpointWithEmptyRoutingKeys();
            Listener listener = context.subscribe();
            Event event = listener.getOne().get(30, TimeUnit.SECONDS);
            if (expectedConnectionKey.equals(event.getRecipientVerkey())) {
                if (event.message() instanceof ConnRequest) {
                    ConnRequest request = (ConnRequest) event.message();
                    Inviter machine = new Inviter(context, me, expectedConnectionKey, myEndpoint);
                    Pairwise pairwise = machine.createConnection(request);
                    if (pairwise == null) {
                        Assert.fail();
                    }
                    context.getPairwiseList().ensureExists(pairwise);
                } else {
                    Assert.fail("Wrong request message type");
                }
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    public static void runInvitee(Context context, Invitation invitation,
                                  String myLabel, Pairwise.Me me) {
        Endpoint myEndpoint = context.getEndpointWithEmptyRoutingKeys();
        // Create and start machine
        Invitee machine = new Invitee(context, me, myEndpoint);
        Pairwise pairwise = machine.createConnection(invitation, myLabel);
        if (pairwise == null) {
            Assert.fail();
        }
        context.getPairwiseList().ensureExists(pairwise);
    }

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testEstablishConnection() throws InterruptedException, ExecutionException, TimeoutException {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams inviter = testSuite.getAgentParams("agent1");
        AgentParams invitee = testSuite.getAgentParams("agent2");

        // Get endpoints
        String connectionKey = null;
        Invitation invitation = null;
        try (Context context = CloudContext.builder().
                setServerUri(inviter.getServerAddress()).
                setCredentials(inviter.getCredentials().getBytes(StandardCharsets.UTF_8)).
                setP2p(inviter.getConnection()).
                build()) {
            String inviterEndpointAddress = context.getEndpointAddressWithEmptyRoutingKeys();
            connectionKey = context.getCrypto().createKey();
            invitation = Invitation.builder().
                    setLabel("Inviter").
                    setEndpoint(inviterEndpointAddress).
                    setRecipientKeys(Collections.singletonList(connectionKey)).
                    build();
        }

        // Init Me
        Pairwise.Me inviterMe = null;
        try (Context context = CloudContext.builder().
                setServerUri(inviter.getServerAddress()).
                setCredentials(inviter.getCredentials().getBytes(StandardCharsets.UTF_8)).
                setP2p(inviter.getConnection()).
                build()) {
            Pair<String, String> didVerkey = context.getDid().createAndStoreMyDid();
            inviterMe = new Pairwise.Me(didVerkey.first, didVerkey.second);
        }
        Pairwise.Me inviteeMe = null;
        try (Context context = CloudContext.builder().
                setServerUri(invitee.getServerAddress()).
                setCredentials(invitee.getCredentials().getBytes(StandardCharsets.UTF_8)).
                setP2p(invitee.getConnection()).
                build()) {
            Pair<String, String> didVerkey = context.getDid().createAndStoreMyDid();
            inviteeMe = new Pairwise.Me(didVerkey.first, didVerkey.second);
        }

        String finalConnectionKey = connectionKey;
        Pairwise.Me finalInviterMe = inviterMe;
        CompletableFuture<Boolean> runInviterFeature = CompletableFuture.supplyAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(inviter.getServerAddress()).
                    setCredentials(inviter.getCredentials().getBytes(StandardCharsets.UTF_8)).
                    setP2p(inviter.getConnection()).
                    build()) {
                runInviter(context, finalConnectionKey, finalInviterMe);
            }
            return true;
        }, r -> new Thread(r).start());

        Invitation finalInvitation = invitation;
        Pairwise.Me finalInviteeMe = inviteeMe;
        CompletableFuture<Boolean> runInviteeFeature = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Assert.fail();
            }
            try (Context context = CloudContext.builder().
                    setServerUri(invitee.getServerAddress()).
                    setCredentials(invitee.getCredentials().getBytes(StandardCharsets.UTF_8)).
                    setP2p(invitee.getConnection()).
                    build()) {
                runInvitee(context, finalInvitation, "Invitee", finalInviteeMe);
            }
            return true;
        }, r -> new Thread(r).start());

        runInviterFeature.get(60, TimeUnit.SECONDS);
        runInviteeFeature.get(60, TimeUnit.SECONDS);
    }

}
