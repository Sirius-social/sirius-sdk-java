package examples.raft.models;

import com.sirius.sdk.messaging.Message;

public class TrustPingMessageUnderTest extends Message {
    public TrustPingMessageUnderTest(String message) {
        super(message);
    }
}
