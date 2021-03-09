import com.sirius.sdk.errors.sirius_exceptions.SiriusPendingOperation;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.rpc.AddressedTunnel;
import com.sirius.sdk.rpc.Future;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import models.P2PModel;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestFuturePromise {

    ConfTest confTest;
    Pair<P2PModel, P2PModel> p2pPair;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
        p2pPair = confTest.createP2P();
    }


    @Test
    public void testSane() {
        AddressedTunnel agent_to_sdk = p2pPair.first.getTunnel();
        AddressedTunnel sdk_to_agent = p2pPair.second.getTunnel();

        Future future = new Future(sdk_to_agent);
        boolean isSiriusPendingOperation = false;
        try {
            future.getValue();
        } catch (SiriusPendingOperation siriusPendingOperation) {
            isSiriusPendingOperation = true;
        }
        Assert.assertTrue(isSiriusPendingOperation);
        String expected = "Test OK";
        JSONObject promiseMsgObj = new JSONObject();
        promiseMsgObj.put("@type", Future.MSG_TYPE);
        promiseMsgObj.put("@id", "promise-message-id");
        promiseMsgObj.put("is_tuple", false);
        promiseMsgObj.put("is_bytes", false);
        promiseMsgObj.put("value", expected);
        promiseMsgObj.put("exception", JSONObject.NULL);
        JSONObject threadObject = new JSONObject();
        threadObject.put("thid", future.promise().getId());
        promiseMsgObj.put("~thread", threadObject);
        Message message = new Message(promiseMsgObj.toString());
        boolean isWait = false;
        try {
            isWait = future.waitPromise(5);
        } catch (Exception ignored) {
        }
        Assert.assertFalse(isWait);

        agent_to_sdk.post(message);
        boolean isOk = future.waitPromise(5);
        Assert.assertTrue(isOk);
        try {
            Object actual = future.getValue();
            Assert.assertEquals(expected, actual.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
