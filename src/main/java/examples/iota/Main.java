package examples.iota;

import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.diddoc.IotaPublicDidDoc;
import com.sirius.sdk.hub.MobileContext;
import com.sirius.sdk.hub.MobileHub;
import com.sirius.sdk.utils.IotaUtils;
import foundation.identity.jsonld.JsonLDException;
import org.iota.client.Client;
import org.iota.client.local.NativeAPI;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

public class Main {

    private static final String MAINNET = "https://chrysalis-nodes.iota.cafe:443";

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

//        String key = context.getCrypto().createKey();
//        System.out.println(key);
//        String msg = "hello world".repeat(50);
//        System.out.println(msg.getBytes(StandardCharsets.UTF_8).length);
//        int s1 = context.getCrypto().packMessage(
//                msg,
//                Arrays.asList(
//                        "2pdpeNC8xh4MWevko1V9Q9WFDeb2qmZSvDFAkELkTrW1"
//                ),
//                key).length;
//
//        System.out.println(s1);
//
//        int s2 = context.getCrypto().packMessage(
//                msg,
//                Arrays.asList(
//                        "2pdpeNC8xh4MWevko1V9Q9WFDeb2qmZSvDFAkELkTrW1",
//                        "2pdpeNC8xh4MWevko1V9Q9WFDeb2qmZSvDFAkELkTrW2",
//                        "2pdpeNC8xh4MWevko1V9Q9WFDeb2qmZSvDFAkELkTrW3",
//                        "2pdpeNC8xh4MWevko1V9Q9WFDeb2qmZSvDFAkELkTrW4",
//                        "2pdpeNC8xh4MWevko1V9Q9WFDeb2qmZSvDFAkELkTrW5"
//                        ),
//                key).length;
//
//        System.out.println(s2);

        //-Djava.library.path=/Users/mike/ssi/iota.rs/bindings/java/target/release
        IotaUtils.iotaNetwork = IotaUtils.TESTNET;
        IotaPublicDidDoc didDoc = new IotaPublicDidDoc(context.getCrypto());
        didDoc.submitToLedger(context);
        System.out.println(didDoc.getDidDoc());
        System.out.println("https://explorer.iota.org/mainnet/identity-resolver/"+didDoc.getDid());

//        IotaPublicDidDoc didDoc2 = IotaPublicDidDoc.load("did:iota:EFUXU487JVtT5T8ouTKyJqtHtRbkg2neoU1VYusmEGGK");
//        System.out.println(didDoc.getDidDoc());
//        System.out.println(didDoc2.getDidDoc());
//
//        didDoc2.addAgentServices(context);
//        System.out.println(didDoc2.getDidDoc());
//        didDoc2.submitToLedger(context);
//
//        IotaPublicDidDoc didDoc3 = IotaPublicDidDoc.load(didDoc.getDid());
//        System.out.println(didDoc3.getDidDoc());

        context.close();
    }
}
