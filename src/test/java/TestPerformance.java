import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.coprotocols.ThreadBasedCoProtocolTransport;
import com.sirius.sdk.agent.pairwise.Pairwise;
import helpers.ConfTest;
import org.junit.Before;

public class TestPerformance {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
       // confTest.getSuiteSingleton().
    }



    public void routineForPinger (CloudAgent agent, Pairwise p , String threadId){
        ThreadBasedCoProtocolTransport transport = agent.spawn(threadId, p);
       

    }
 /*   async def routine_for_pinger(agent: Agent, p: Pairwise, thread_id: str):
    transport = await agent.spawn(thread_id, p)
    await transport.start()
    try:
            for n in range(TEST_ITERATIONS):
    ping = Message({
        '@id': 'message-id-' + uuid.uuid4().hex,
                '@type': 'did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test/1.0/ping',
                "comment": "Hi",
    })
    ok, pong = await transport.switch(ping)
            assert ok
            assert pong['@id'] == ping['@id']
            finally:
    await transport.stop()*/
}
