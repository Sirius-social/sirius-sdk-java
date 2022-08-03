package examples.iota;

import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.diddoc.IotaPublicDidDoc;
import com.sirius.sdk.hub.MobileContext;
import com.sirius.sdk.hub.MobileHub;
import foundation.identity.jsonld.JsonLDException;
import org.iota.client.Client;
import org.iota.client.local.NativeAPI;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class Main {

    private static final String MAINNET = "https://chrysalis-nodes.iota.cafe:443";

    static {
        NativeAPI.verifyLink();
    }

    private static Client node() {
        Client iota = Client.Builder().withNode(MAINNET)
                .finish();
        return iota;
    }

    public static void main(String[] args) throws JsonLDException, GeneralSecurityException, IOException {
        MobileHub.Config mobileConfig = new MobileHub.Config();
        JSONObject walletConfig = new JSONObject().
                put("id", "Wallet9").
                put("storage_type", "default");
        JSONObject walletCredentials = new JSONObject().
                put("key", "8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY").
                put("key_derivation_method", "RAW");
        mobileConfig.walletConfig = walletConfig;
        mobileConfig.walletCredentials = walletCredentials;
        mobileConfig.mediatorInvitation = Invitation.builder().
                setLabel("Mediator").
                setEndpoint("ws://mediator.socialsirius.com:8000/ws").
                setRecipientKeys(Collections.singletonList("DjgWN49cXQ6M6JayBkRCwFsywNhomn8gdAXHJ4bb98im")).
                build();

        MobileContext context = new MobileContext(mobileConfig);

        IotaPublicDidDoc didDoc = new IotaPublicDidDoc(context.getCrypto());
        didDoc.submitToLedger(context);

        IotaPublicDidDoc didDoc2 = IotaPublicDidDoc.load(didDoc.getDid());
        System.out.println(didDoc.getDidDoc());
        System.out.println(didDoc2.getDidDoc());

        didDoc2.addAgentServices(context);
        System.out.println(didDoc2.getDidDoc());
        didDoc2.submitToLedger(context);

        IotaPublicDidDoc didDoc3 = IotaPublicDidDoc.load(didDoc.getDid());
        System.out.println(didDoc3.getDidDoc());

        context.close();
    }
}
