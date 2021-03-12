import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.Event;
import com.sirius.sdk.agent.Listener;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.errors.sirius_exceptions.SiriusRPCError;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import models.TrustPingMessageUnderTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class TestAgent {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    //TODO do all tests
    @Test
    public void testAllAgentsPing() {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        List<String> allAgentsList = new ArrayList<>();
        allAgentsList.add("agent1");
        allAgentsList.add("agent2");
        allAgentsList.add("agent3");
        allAgentsList.add("agent4");
        for (int i = 0; i < allAgentsList.size(); i++) {
            String agentName = allAgentsList.get(i);
            AgentParams params = testSuite.getAgentParams(agentName);
            Agent agent = new Agent(params.getServerAddress(), params.getCredentials().getBytes(StandardCharsets.US_ASCII),
                    params.getConnection(), 10);
            agent.open();
            boolean isPinged = agent.ping();
            Assert.assertTrue(isPinged);
            agent.close();
        }
    }

    @Test
    public void testAgentsWallet() {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams params = testSuite.getAgentParams("agent1");
        Agent agent = new Agent(params.getServerAddress(), params.getCredentials().getBytes(StandardCharsets.US_ASCII),
                params.getConnection(), 10);
        agent.open();
        //Check wallet calls is ok
        Pair<String, String> didVerkey = agent.getWallet().getDid().createAndStoreMyDid();
        Assert.assertNotNull(didVerkey);
        Assert.assertNotNull(didVerkey.first);
        Assert.assertNotNull(didVerkey.second);
        agent.close();
    }

    @Test
    public void testAgentsCommunications() throws InterruptedException, ExecutionException, TimeoutException, SiriusRPCError {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams agent1params = testSuite.getAgentParams("agent1");
        AgentParams agent2params = testSuite.getAgentParams("agent2");
        List<Entity> entityList1 = agent1params.getEntitiesList();
        List<Entity> entityList2 = agent2params.getEntitiesList();
        Entity entity1 = entityList1.get(0);
        Entity entity2 = entityList2.get(0);
        Agent agent1 = new Agent(agent1params.getServerAddress(), agent1params.getCredentials().getBytes(StandardCharsets.US_ASCII),
                agent1params.getConnection(), 10);
        Agent agent2 = new Agent(agent2params.getServerAddress(), agent2params.getCredentials().getBytes(StandardCharsets.US_ASCII),
                agent2params.getConnection(), 10);
        agent1.open();
        agent2.open();
        //Get endpoints
        String agent2Endpoint = "";
        for (Endpoint e : agent2.getEndpoints()) {
            if (e.getRoutingKeys().size() == 0) {
                agent2Endpoint = e.getAddress();
                break;
            }

        }


        agent1.getWallet().getDid().storeTheirDid(entity2.getDid(), entity2.getVerkey());
        if (!agent1.getWallet().getPairwise().isPairwiseExist(entity2.getDid())) {
            System.out.println("#1");
            agent1.getWallet().getPairwise().createPairwise(entity2.getDid(), entity1.getDid());
        }
        agent2.getWallet().getDid().storeTheirDid(entity1.getDid(), entity1.getVerkey());
        if (!agent2.getWallet().getPairwise().isPairwiseExist(entity1.getDid())) {
            System.out.println("#2");
            agent2.getWallet().getPairwise().createPairwise(entity1.getDid(), entity2.getDid());
        }
        //Prepare Message
        Message trustPing = new Message(new JSONObject().
                put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/trust_ping/1.0/ping").
                put("@id", "trust-ping-message" + UUID.randomUUID().hashCode()).
                put("comment", "Hi. Are you listening?").
                put("response_requested", true));
        List<String> thierVerkeys = new ArrayList<>();
        thierVerkeys.add(entity2.getVerkey());
        String finalAgent2Endpoint = agent2Endpoint;


        Listener agent2Listener = agent2.subscribe();
        CompletableFuture<Event> eventFeat = agent2Listener.getOne();
        System.out.println("sendMess1=");
        agent1.sendMessage(trustPing, thierVerkeys, finalAgent2Endpoint, entity1.getVerkey(), new ArrayList<>());

        Event event = eventFeat.get(10, TimeUnit.SECONDS);
        System.out.println("event=" + event.getMessageObj());
        JSONObject message = event.getJSONOBJECTFromJSON("message");
        Assert.assertNotNull(message);
        String type = message.getString("@type");
        Assert.assertEquals("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/trust_ping/1.0/ping", type);
        String id = message.getString("@id");
        Assert.assertEquals(trustPing.getId(), id);


        agent1.close();
        agent2.close();


    }

    @Test
    public void testListenerRestoreMessage() throws InterruptedException, ExecutionException, TimeoutException, SiriusRPCError {
        AgentParams agent1Params = confTest.getSuiteSingleton().getAgentParams("agent1");
        AgentParams agent2Params = confTest.getSuiteSingleton().getAgentParams("agent2");
        List<Entity> agent1ParamsEntitiesList = agent1Params.getEntitiesList();
        List<Entity> agent2ParamsEntitiesList = agent2Params.getEntitiesList();
        Entity entity1 = agent1ParamsEntitiesList.get(0);
        Entity entity2 = agent2ParamsEntitiesList.get(0);
        Agent agent1 = new Agent(agent1Params.getServerAddress(), agent1Params.getCredentials().
                getBytes(StandardCharsets.US_ASCII), agent1Params.getConnection(), 10);
        Agent agent2 = new Agent(agent2Params.getServerAddress(), agent2Params.getCredentials().
                getBytes(StandardCharsets.US_ASCII), agent2Params.getConnection(), 10);

        agent1.open();
        agent2.open();

        //GET endpoints
        String agent2Endpoint = null;
        for (int i = 0; i < agent2.getEndpoints().size(); i++) {
            if (agent2.getEndpoints().get(i).getRoutingKeys().isEmpty()) {
                agent2Endpoint = agent2.getEndpoints().get(i).getAddress();
            }
        }
        Listener agent2Listener = agent2.subscribe();

        //# Exchange Pairwise
        agent1.getWallet().getDid().storeTheirDid(entity2.getDid(), entity2.getVerkey());
        boolean isExist1 = agent1.getWallet().getPairwise().isPairwiseExist(entity2.getDid());
        if (!isExist1) {
            System.out.println("#1");
            agent1.getWallet().getPairwise().createPairwise(entity2.getDid(), entity1.getDid());
        }

        agent2.getWallet().getDid().storeTheirDid(entity1.getDid(), entity1.getVerkey());
        boolean isExist2 = agent2.getWallet().getPairwise().isPairwiseExist(entity1.getDid());
        if (!isExist2) {
            System.out.println("#2");
            agent2.getWallet().getPairwise().createPairwise(entity1.getDid(), entity2.getDid());
        }

        //Bind Message class to protocol
        Message.registerMessageClass(TrustPingMessageUnderTest.class, "trust_ping_test");
        //Prepare message
        Message trust_ping = new Message(new JSONObject().
                put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/trust_ping_test/1.0/ping").
                put("@id", "trust-ping-message" + UUID.randomUUID().hashCode()).
                put("comment", "Hi. Are you listening?").
                put("response_requested", true));
        List<String> verkeyList = new ArrayList<>();
        verkeyList.add(entity2.getVerkey());

        CompletableFuture<Event> eventFeat = agent2Listener.getOne();
        agent1.sendMessage(trust_ping, verkeyList, agent2Endpoint, entity1.getVerkey(), new ArrayList<>());


        Event event = eventFeat.get(10, TimeUnit.SECONDS);
        JSONObject message = event.getJSONOBJECTFromJSON("message");
        System.out.println("message=" + message);
           // assert isinstance(msg, TrustPingMessageUnderTest), 'Unexpected msg type: ' + str(type(msg))
        agent1.close();
        agent2.close();
    }

}
