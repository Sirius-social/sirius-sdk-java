import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.n_wise.IotaNWise;
import com.sirius.sdk.agent.n_wise.NWise;
import com.sirius.sdk.agent.n_wise.NWiseList;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestNWise {
    ConfTest confTest;

    static {
        IotaUtils.iotaNetwork = IotaUtils.TESTNET;
    }

    static final String alice = "agent1";
    static final String bob = "agent2";
    static final String carol = "agent3";

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
        String chatName = "test chat";

        Invitation invitationForBob;
        Invitation invitationForCarol;

        IotaNWise aliceChat = null;
        try (Context context = getContext(alice)) {
            aliceChat = IotaNWise.createChat(chatName, "Alice", context);
            invitationForBob = aliceChat.createInvitation(context);
            invitationForCarol = aliceChat.createInvitation(context);
            new NWiseList(context.getNonSecrets()).add(aliceChat);
        }

        IotaNWise finalAliceChat = aliceChat;
        Thread aliceThread = new Thread(() -> {
            Listener listener = null;
            try (Context context = getContext(alice)) {
                listener = context.subscribe();
                System.out.println("Start listening...");
                for (int i = 0; i < 3; i++) {
                    Event event = listener.getOne().get(30, TimeUnit.SECONDS);
                    System.out.println("Event:" + event.message());
                    if (finalAliceChat.getCurrentParticipantsVerkeysBase58().contains(event.getRecipientVerkey())) {
                        if (event.message() instanceof Message) {
                            Message message = (Message) event.message();
                            finalAliceChat.fetchFromLedger();
                            String nick = finalAliceChat.resolveNickname(event.getSenderVerkey());
                            Assert.assertEquals("Carol", nick);
                            System.out.println("New message from " + nick + " : " + message.getContent());
                        }
                    }
                    if (event.message() instanceof Request) {
                        Assert.assertTrue(finalAliceChat.acceptRequest((Request) event.message(), event.getRecipientVerkey(), context));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                listener.unsubscribe();
            }
        });
        aliceThread.start();

        IotaNWise bobChat = null;
        try (Context context = getContext(bob)) {
            bobChat = IotaNWise.acceptInvitation(invitationForBob, "Bob", context);
            Assert.assertNotNull(bobChat);
        }

        IotaNWise carolChat = null;
        try (Context context = getContext(carol)) {
            carolChat = IotaNWise.acceptInvitation(invitationForCarol, "Carol", context);
            Assert.assertNotNull(carolChat);
            carolChat.send(Message.builder().setContent("Hello world").build(), context);
        }

        Assert.assertEquals(3, aliceChat.getParticipants().size());
        Assert.assertEquals(3, bobChat.getParticipants().size());
        Assert.assertEquals(3, carolChat.getParticipants().size());

        Assert.assertEquals(chatName, aliceChat.getChatName());
        Assert.assertEquals(chatName, bobChat.getChatName());
        Assert.assertEquals(chatName, carolChat.getChatName());

        bobChat.leave();
        carolChat.fetchFromLedger();
        Assert.assertEquals(2, carolChat.getParticipants().size());

        aliceChat.removeParticipant(carolChat.getMyDid());
        Assert.assertEquals(1, aliceChat.getParticipants().size());

        aliceThread.interrupt();
    }

    @Test
    public void testRestoreIotaChat() {
        IotaNWise aliceChat1 = null;
        IotaNWise aliceChat2 = null;
        try (Context context = getContext(alice)) {
            aliceChat1 = IotaNWise.createChat("chat1", "Alice", context);
            aliceChat2 = IotaNWise.createChat("chat2", "Alice", context);
            new NWiseList(context.getNonSecrets()).add(aliceChat1);
            new NWiseList(context.getNonSecrets()).add(aliceChat2);
        }

        try (Context context = getContext(alice)) {
            List<NWiseList.NWiseInfo> nWiseList = new NWiseList(context.getNonSecrets()).getNWiseInfoList();
            Assert.assertEquals(2, nWiseList.size());
            NWise.restore(nWiseList.get(0));
        }
    }
}
