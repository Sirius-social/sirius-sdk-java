import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.model.Endpoint;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;


public class TestAriesFeature0048 {

    ConfTest confTest;

    static String getFirstEndpointAddressWIthEmptyRoutingKeys(Agent agent) {
        for (Endpoint e : agent.getEndpoints()) {
            if (e.getRoutingKeys().size() == 0) {
                return e.getAddress();
            }
        }

        return "";
    }

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testEstablishConnection() {
        Agent agent1 = confTest.getAgent("agent1");
        Agent agent2 = confTest.getAgent("agent2");
        Agent agent3 = confTest.getAgent("agent3");

        agent1.open();
        agent2.open();
        agent3.open();

        Pair<String,String> didVerkey1 = agent1.getWallet().getDid().createAndStoreMyDid();
        Pair<String,String> didVerkey2 = agent2.getWallet().getDid().createAndStoreMyDid();

        String endpointAddress2 = getFirstEndpointAddressWIthEmptyRoutingKeys(agent2);
        String endpointAddress3 = getFirstEndpointAddressWIthEmptyRoutingKeys(agent3);

        agent1.getWallet().getDid().storeTheirDid(didVerkey2.first, didVerkey2.second);
        agent1.getWallet().getPairwise().createPairwise(didVerkey2.first, didVerkey1.first);
        agent2.getWallet().getDid().storeTheirDid(didVerkey1.first, didVerkey1.second);
        agent1.getWallet().getPairwise().createPairwise(didVerkey1.first, didVerkey2.first);

        Pairwise to = new Pairwise(new Pairwise.Me(didVerkey1.first, didVerkey1.second),
                new Pairwise.Their(didVerkey2.first, "Agent2", endpointAddress2, didVerkey2.second));

        Ping ping = new Ping("testMsg");

        agent1.sendTo(ping, to);

        //agent1.sendMessage()

    }

}
