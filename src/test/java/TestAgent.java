import com.sirius.sdk.agent.Agent;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

}
