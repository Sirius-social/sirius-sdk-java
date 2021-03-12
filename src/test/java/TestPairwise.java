import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestPairwise {
    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testPairwiseList() {
        Agent agent1 = confTest.agent1();
        Agent agent2 = confTest.agent2();

        agent1.open();
        agent2.open();

        Pair<String,String> did1Verkey1 = agent1.getWallet().getDid().createAndStoreMyDid();
        Pair<String,String> did2Verkey2 = agent2.getWallet().getDid().createAndStoreMyDid();

        JSONObject metaObj = new JSONObject();
        metaObj.put("test","test-value");
        Pairwise pairwise = new Pairwise(new Pairwise.Me(did1Verkey1.first,did1Verkey1.second),
                new Pairwise.Their(did2Verkey2.first,"Test-Pairwise","http://endpoint",did2Verkey2.second),metaObj);


        List<Object> list1 = agent1.getWallet().getPairwise().listPairwise();
        agent1.getPairwiseList().ensureExists(pairwise);
        List<Object> list2 = agent1.getWallet().getPairwise().listPairwise();
        Assert.assertTrue(list1.size()<list2.size());

       boolean ok =  agent1.getPairwiseList().isExists(did2Verkey2.first);
        Assert.assertTrue(ok);

        Pairwise pairwise2 =   agent1.getPairwiseList().loadForVerkey(did2Verkey2.second);
        Assert.assertEquals(pairwise.getMetadata().toString(),pairwise2.getMetadata().toString());

        agent1.close();
        agent2.close();
    }

}
