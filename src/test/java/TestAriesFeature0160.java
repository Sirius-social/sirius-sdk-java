import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestAriesFeature0160 {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testEstablishConnection() {
        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams agent1params = testSuite.getAgentParams("agent1");
        AgentParams agent2params = testSuite.getAgentParams("agent2");
    }

}
