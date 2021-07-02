import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.errors.sirius_exceptions.SiriusRPCError;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class TestAriesFeature0048 {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testEstablishConnection() throws InterruptedException, ExecutionException, TimeoutException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, SiriusRPCError {
        CloudAgent agent1 = confTest.getAgent("agent1");
        CloudAgent agent2 = confTest.getAgent("agent2");
        CloudAgent agent3 = confTest.getAgent("agent3");

        agent1.open();
        agent2.open();
        agent3.open();

        Pair<String,String> didVerkey1 = agent1.getWallet().getDid().createAndStoreMyDid();
        Pair<String,String> didVerkey2 = agent2.getWallet().getDid().createAndStoreMyDid();

        String endpointAddress2 = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent2);
        String endpointAddress3 = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent3);

        agent1.getWallet().getDid().storeTheirDid(didVerkey2.first, didVerkey2.second);
        agent1.getWallet().getPairwise().createPairwise(didVerkey2.first, didVerkey1.first);
        agent2.getWallet().getDid().storeTheirDid(didVerkey1.first, didVerkey1.second);
        agent2.getWallet().getPairwise().createPairwise(didVerkey1.first, didVerkey2.first);

        Pairwise to = new Pairwise(new Pairwise.Me(didVerkey1.first, didVerkey1.second),
                new Pairwise.Their(didVerkey2.first, "Agent2", endpointAddress2, didVerkey2.second));

        Listener listener2 = agent2.subscribe();
        Ping ping = Ping.builder().
                setComment("testMsg").
                setResponseRequested(false).
                build();

        Future<Event> feature2 = listener2.getOne();
        agent1.sendTo(ping, to);

        // Check OK
        Event event = feature2.get(10, TimeUnit.SECONDS);
        JSONObject recv = event.getJSONOBJECTFromJSON("message");
        Pair<Boolean, Message> result = Message.restoreMessageInstance(recv.toString());
        Assert.assertTrue(result.first);
        Assert.assertTrue(result.second instanceof Ping);
        Assert.assertEquals(((Ping) result.second).getComment(), ping.getComment());

        // Check Error
        boolean thrown = false;
        to = new Pairwise(new Pairwise.Me(didVerkey1.first, didVerkey1.second),
                new Pairwise.Their(didVerkey2.first, "Agent3", endpointAddress3, didVerkey2.second));
        agent1.sendTo(ping, to);
    }

}
