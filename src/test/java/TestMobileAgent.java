import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.MobileAgent;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.model.Entity;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestMobileAgent {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testSendMessage() throws InterruptedException, ExecutionException, TimeoutException {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();

        CloudAgent cloudAgent = confTest.getAgent("agent1");
        cloudAgent.open();
        AgentParams cloudAgentParams = testSuite.getAgentParams("agent1");
        List<Entity> entityList = cloudAgentParams.getEntitiesList();
        Entity entity = entityList.get(0);

        String cloudAgentEndpoint = "";
        for (Endpoint e : cloudAgent.getEndpoints()) {
            if (e.getRoutingKeys().size() == 0) {
                cloudAgentEndpoint = e.getAddress();
                break;
            }
        }

        JSONObject walletConfig = new JSONObject().
                put("id", "Wallet1").
                put("storage_type", "default");
        JSONObject walletCredentials = new JSONObject().
                put("key", "8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY").
                put("key_derivation_method", "RAW");
        MobileAgent mobileAgent = new MobileAgent(walletConfig, walletCredentials);
        mobileAgent.open();

        Message msg = Message.builder().
                setContent("Test").
                setLocale("en").
                build();

        Listener cloudListener = cloudAgent.subscribe();
        CompletableFuture<Event> cf = cloudListener.getOne();

        mobileAgent.sendMessage(msg, Arrays.asList(entity.getVerkey()), cloudAgentEndpoint, null, new ArrayList<>());

        Event event = cf.get(30, TimeUnit.SECONDS);
        Assert.assertTrue(event.message() instanceof Message);
        Assert.assertEquals(((Message) event.message()).getContent(), msg.getContent());

        cloudAgent.close();
    }
}
