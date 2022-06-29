import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.n_wise.IotaChat;
import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.agent.n_wise.messages.Request;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.IotaUtils;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class TestNWise {
    ConfTest confTest;

    static {
        IotaUtils.iotaNetwork = IotaUtils.TESTNET;
    }

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    public Context getContext(String agentName) {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams agent = testSuite.getAgentParams(agentName);
        return CloudContext.builder().
                setServerUri(agent.getServerAddress()).
                setCredentials(agent.getCredentials().getBytes(StandardCharsets.UTF_8)).
                setP2p(agent.getConnection()).
                build();
    }

    @Test
    public void testIotaChat() {
        String alice = "agent1";
        String bob = "agent2";
        String carol = "agent3";

        Invitation invitationForBob;
        Invitation invitationForCarol;

        IotaChat aliceChat = null;
        try (Context context = getContext(alice)) {
            aliceChat = IotaChat.createChat("Iota chat", "Alice", context);
            invitationForBob = aliceChat.createInvitation(context);
            invitationForCarol = aliceChat.createInvitation(context);
        }


        IotaChat finalAliceChat = aliceChat;
        Thread aliceThread = new Thread(() -> {
            Listener listener = null;
            try (Context context = getContext(alice)) {
                listener = context.subscribe();
                for (int i = 0; i < 3; i++) {
                    Event event = listener.getOne().get(30, TimeUnit.SECONDS);
                    System.out.println("Event:" + event.message());
                    if ((event.getRecipientVerkey().equals(invitationForBob.getInviterVerkey()) ||
                            event.getRecipientVerkey().equals(invitationForCarol.getInviterVerkey()))) {
                        if (event.message() instanceof Request) {
                            Assert.assertTrue(finalAliceChat.acceptRequest((Request) event.message(), context));
                        }
                    }
                    if (event.message() instanceof Message) {
                        Message message = (Message) event.message();
                        String nick = finalAliceChat.resolveNickname(event.getSenderVerkey());
                        System.out.println("New message from " + nick + " : " + message.getContent());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                listener.unsubscribe();
            }
        });
        aliceThread.start();

        IotaChat bobChat = null;
        try (Context context = getContext(bob)) {
            bobChat = IotaChat.acceptInvitation(invitationForBob, "Bob", context);
            Assert.assertNotNull(bobChat);
            bobChat.send(Message.builder().setContent("Hello world").build(), context);
        }

        IotaChat carolChat = null;
        try (Context context = getContext(carol)) {
            carolChat = IotaChat.acceptInvitation(invitationForCarol, "Bob", context);
            Assert.assertNotNull(carolChat);
        }

        Assert.assertEquals(3, aliceChat.getParticipants().size());
        Assert.assertEquals(3, bobChat.getParticipants().size());
        Assert.assertEquals(3, carolChat.getParticipants().size());

        aliceThread.interrupt();
    }
}
