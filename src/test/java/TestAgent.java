import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.Event;
import com.sirius.sdk.agent.Listener;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

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
                    params.getConnection(), 5, null, null);
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
                params.getConnection(), 5, null, null);
        agent.open();
        //Check wallet calls is ok
        Pair<String, String> didVerkey = agent.getWallet().getDid().createAndStoreMyDid(null, null, null);
        Assert.assertNotNull(didVerkey);
        Assert.assertNotNull(didVerkey.first);
        Assert.assertNotNull(didVerkey.second);
        agent.close();
    }

    @Test
    public void testAgentsCommunications() {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams agent1params = testSuite.getAgentParams("agent1");
        AgentParams agent2params = testSuite.getAgentParams("agent2");
        Set<String> keySet1 = agent1params.getEntitiesObject().keySet();
        Set<String> keySet2 = agent2params.getEntitiesObject().keySet();
        JSONObject entity1 = new JSONObject();
        for (String key : keySet1) {
            entity1 = agent1params.getEntitiesObject().getJSONObject(key);
            break;
        }
        JSONObject entity2 = new JSONObject();
        for (String key : keySet2) {
            entity2 = agent2params.getEntitiesObject().getJSONObject(key);
            break;
        }

        System.out.println(entity1);
        Agent agent1 = new Agent(agent1params.getServerAddress(), agent1params.getCredentials().getBytes(StandardCharsets.US_ASCII),
                agent1params.getConnection(), 10, null, null);
        Agent agent2 = new Agent(agent2params.getServerAddress(), agent2params.getCredentials().getBytes(StandardCharsets.US_ASCII),
                agent2params.getConnection(), 10, null, null);
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
        Listener agent2Listener = agent2.subscribe();

        agent1.getWallet().getDid().storeTheirDid(entity2.getString("did"), entity2.getString("verkey"));
        if (!agent1.getWallet().getPairwise().isPairwiseExist(entity2.getString("did"))) {
            System.out.println("#1");
            agent1.getWallet().getPairwise().createPairwise(entity2.getString("did"), entity1.getString("did"), null, null);
        }
        agent2.getWallet().getDid().storeTheirDid(entity1.getString("did"), entity1.getString("verkey"));
        if (!agent2.getWallet().getPairwise().isPairwiseExist(entity1.getString("did"))) {
            System.out.println("#2");
            agent1.getWallet().getPairwise().createPairwise(entity1.getString("did"), entity2.getString("did"), null, null);
        }
        //Prepare Message
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("@id", "trust-ping-message" + UUID.randomUUID().hashCode());
        jsonObject.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/trust_ping/1.0/ping");
        jsonObject.put("comment", "Hi. Are you listening?");
        jsonObject.put("response_requested", true);
        Message trustPing = new Message(jsonObject.toString());
        List<String> thierVerkeys = new ArrayList<>();
        thierVerkeys.add(entity2.getString("verkey"));
        agent1.sendMessage(trustPing, thierVerkeys, agent2Endpoint, entity1.getString("verkey"), new ArrayList<>());
        Event event = agent2Listener.getOne(5);
        JSONObject message = event.getJSONOBJECTFromJSON("message");
        Assert.assertNotNull(message);
        String type = message.getString("@type");
        Assert.assertEquals("did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/trust_ping/1.0/ping", type);
        String id = message.getString("@id");
        Assert.assertEquals(trustPing.getId(), id);
        agent1.close();
        agent2.close();
    }

}
