import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Ping;
import com.sirius.sdk.agent.aries_rfc.feature_0048_trust_ping.Pong;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import models.TestMessage1;
import models.TestMessage2;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class TestMessages {

    @Test
    public void testRegisterProtocolMessageSuccess() {
        try {
            Message.registerMessageClass(TestMessage1.class, "test-protocol");
            JSONObject messObject = new JSONObject();
            messObject.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test-protocol/1.0/name");
            Pair<Boolean, Message> result = Message.restoreMessageInstance(messObject.toString());
            Assert.assertTrue(result.first);
            Assert.assertTrue(result.second instanceof TestMessage1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }


    }


    @Test
    public void testRegisterProtocolMessageFail() {
        Message.registerMessageClass(TestMessage1.class, "test-protocol");
        JSONObject messObject = new JSONObject();
        messObject.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/fake-protocol/1.0/name");
        try {
            Pair<Boolean, Message> result = Message.restoreMessageInstance(messObject.toString());
            Assert.assertFalse(result.first);
            Assert.assertNull(result.second);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRegisterProtocolMessageMultipleName() {
        Message.registerMessageClass(TestMessage1.class, "test-protocol");
        Message.registerMessageClass(TestMessage2.class, "test-protocol", "test-name");
        try {
            JSONObject messObject = new JSONObject();
            messObject.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test-protocol/1.0/name");
            Pair<Boolean, Message> result = Message.restoreMessageInstance(messObject.toString());
            Assert.assertTrue(result.first);
            Assert.assertTrue(result.second instanceof TestMessage1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        try {
            JSONObject messObject = new JSONObject();
            messObject.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/test-protocol/1.0/test-name");
            Pair<Boolean, Message> result = Message.restoreMessageInstance(messObject.toString());
            Assert.assertTrue(result.first);
            Assert.assertTrue(result.second instanceof TestMessage2);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAriesPingPong() {

        JSONObject pingObject = new JSONObject();
        pingObject.put("@id", "trust-ping-message-id");
        pingObject.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/trust_ping/1.0/ping");
        pingObject.put("comment", "Hi. Are you OK?");
        pingObject.put("response_requested", true);
        Ping ping = new Ping(pingObject.toString());

        try {
            Pair<Boolean, Message> result = Message.restoreMessageInstance(pingObject.toString());
            Assert.assertTrue(result.first);
            Assert.assertTrue(result.second instanceof Ping);
            Assert.assertEquals("Hi. Are you OK?", ((Ping) result.second).getComment());
            Assert.assertTrue(((Ping) result.second).getResponseRequested());

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        JSONObject pongObject = new JSONObject();
        pongObject.put("@id", "trust-ping_response-message-id");
        pongObject.put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/trust_ping/1.0/ping_response");
        pongObject.put("comment", "Hi. I am OK!");
        JSONObject threadObj = new JSONObject();
        threadObj.put("thid", "ping-id");
        pongObject.put("~thread", threadObj);

        Pong pong = new Pong(pongObject.toString());


        try {
            Pair<Boolean, Message> result = Message.restoreMessageInstance(pongObject.toString());
            Assert.assertTrue(result.first);
            Assert.assertTrue(result.second instanceof Pong);
            Assert.assertEquals("Hi. I am OK!", ((Pong) result.second).getComment());
            Assert.assertEquals("ping-id",((Pong) result.second).getThreadId());

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testAriesAck(){
        //TODO make test ack 
     //   Ack message = new Ack();
    }

/*    def test_aries_ack():

    message = Ack(thread_id='ack-thread-id', status=AckStatus.PENDING)
    assert message.protocol == 'notification'
            assert message.name == 'ack'
            assert message.version == '1.0'
            assert str(message.version_info) == '1.0.0'
            assert message.status == AckStatus.PENDING
    message.validate()

    ok, ack = restore_message_instance(
    {
        '@id': 'ack-message-id',
            '@type': 'did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/notification/1.0/ack',
            'status': 'PENDING',
            "~thread": {
        'thid': 'thread-id'
    },
    }
    )
            assert ok is True
    assert isinstance(ack, Ack)
    assert ack.thread_id == 'thread-id'
            ack.validate()
            assert ack.status == AckStatus.PENDING*/
}



