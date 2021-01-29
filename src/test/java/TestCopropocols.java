import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.agent.model.coprotocols.AbstractCoProtocolTransport;
import com.sirius.sdk.agent.model.coprotocols.TheirEndpointCoProtocolTransport;
import com.sirius.sdk.agent.model.pairwise.TheirEndpoint;
import com.sirius.sdk.messaging.Message;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class TestCopropocols {

    static final String[] TEST_MSG_TYPES = {
            "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test_protocol/1.0/request-1",
            "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test_protocol/1.0/response-1",
            "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test_protocol/1.0/request-2",
            "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test_protocol/1.0/response-2"
    };

    ConfTest confTest;
    ServerTestSuite testSuite;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
        testSuite = confTest.getSuiteSingleton();
    }

    void routine1(AbstractCoProtocolTransport protocol) {
        Message firstReq = (new Message.MessageBuilder(TEST_MSG_TYPES[0])).add("content", "Request1").build();
        //protocol.switch
    }

    @Test
    public void testTheirEndpointProtocol() {
        AgentParams agent1params = testSuite.getAgentParams("agent1");
        AgentParams agent2params = testSuite.getAgentParams("agent2");

        Entity entity1 = agent1params.getEntitiesList().get(0);
        Entity entity2 = agent2params.getEntitiesList().get(0);

        Agent agent1 = confTest.getAgent("agent1");
        Agent agent2 = confTest.getAgent("agent2");

        agent1.open();
        agent2.open();

        String agent1Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent1);
        String agent2Endpoint = ServerTestSuite.getFirstEndpointAddressWIthEmptyRoutingKeys(agent2);

        TheirEndpoint their1 = new TheirEndpoint(agent2Endpoint, entity2.getVerkey());
        TheirEndpointCoProtocolTransport agent1Protocol = agent1.spawn(entity1.getVerkey(), their1);
        TheirEndpoint their2 = new TheirEndpoint(agent1Endpoint, entity1.getVerkey());
        TheirEndpointCoProtocolTransport agent2Protocol = agent2.spawn(entity2.getVerkey(), their2);

        agent1Protocol.start(Collections.singletonList("test_protocol"));
        agent2Protocol.start(Collections.singletonList("test_protocol"));




    }
}
