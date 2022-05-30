package examples.iota;

import com.danubetech.keyformats.crypto.ByteSigner;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.hub.MobileContext;
import com.sirius.sdk.hub.MobileHub;
import com.sirius.sdk.utils.Pair;
import foundation.identity.jsonld.JsonLDObject;
import foundation.identity.jsonld.JsonLDException;
import info.weboftrust.ldsignatures.signer.JcsEd25519Signature2020LdSigner;
import info.weboftrust.ldsignatures.signer.LdSigner;
import org.iota.client.Client;
import org.iota.client.local.NativeAPI;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

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
        Endpoint endpoint = new Endpoint("address", Arrays.asList("key1"));
        didDoc.setEndpoint(endpoint);
        didDoc.submit();

        IotaPublicDidDoc didDoc2 = IotaPublicDidDoc.load(didDoc.getDid());
        assert didDoc.getPayload().equals(didDoc2.getPayload());
    }
}
