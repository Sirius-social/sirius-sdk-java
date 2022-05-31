package examples;

import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.CloudHub;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainTest {

    static Context context;


    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CloudHub.Config config = new CloudHub.Config();
        config.serverUri = "https://demo.socialsirius.com";
        config.credentials = "ez8ucxfrTiV1hPX99MHt/JZL1h63sUO9saQCgn2BsaC2EndwDSYpOo6eFpn8xP8ZDoj5B5KN4aaLiyzTqkrbDxrbAe/+2uObPTl6xZdXMBs=".getBytes(StandardCharsets.UTF_8);
        config.p2p = new P2PConnection("B1n1Hwj1USs7z6FAttHCJcqhg7ARe7xtcyfHJCdXoMnC",
                "y7fwmKxfatm6SLN6sqy6LFFjKufgzSsmqA2D4WZz55Y8W7JFeA3LvmicC36E8rdHoAiFhZgSf4fuKmimk9QyBec",
                "5NUzoX1YNm5VXsgzudvVikN7VQpRf5rhaTnPxyu12eZC");

        context = new CloudContext(config);
        String connectionKey = context.getCrypto().createKey();
    }
}
