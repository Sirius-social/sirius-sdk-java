import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.diddoc.IotaPublicDidDoc;
import com.sirius.sdk.agent.n_wise.IotaChat;
import com.sirius.sdk.agent.n_wise.messages.Invitation;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class TestNWise {
    ConfTest confTest;

    static {
        IotaPublicDidDoc.setIotaNetwork(IotaPublicDidDoc.TESTNET);
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
        Invitation invitation;

        try (Context context = getContext("agent1")) {
            IotaChat chat = IotaChat.createChat("Iota chat", "Alice", context);
            invitation = chat.createInvitation(context);
        }

        try (Context context = getContext("agent2")) {
            IotaChat chat = IotaChat.accept(invitation);
            chat.send(Message.builder().setContent("Hello world").build());
        }
    }
}
