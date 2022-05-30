package examples.iota;

import com.danubetech.keyformats.crypto.ByteSigner;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.hub.MobileContext;
import com.sirius.sdk.hub.MobileHub;
import com.sirius.sdk.utils.Pair;
import foundation.identity.jsonld.JsonLDException;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.signer.JcsEd25519Signature2020LdSigner;
import info.weboftrust.ldsignatures.signer.LdSigner;
import jakarta.json.JsonArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;

public class Main {

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

        Pair<String, String> didVk = context.getDid().createAndStoreMyDid();
        ByteSigner byteSigner = new IndyWalletSigner(context.getCrypto(), didVk.second);

        LdSigner ldSigner = new JcsEd25519Signature2020LdSigner(byteSigner);
        ldSigner.setCreated(new Date());

        JSONArray capabilityInvocation = new JSONArray();
        capabilityInvocation.add(new JSONObject()
                .put("id", "did:iota:" + didVk.first + "#sign-0")
                .put("controller", "did:iota:" + didVk.first)
                .put("type", "Ed25519VerificationKey2018")
        );
        JSONObject didDoc = new JSONObject()
                .put("id", "did:iota:" + didVk.first)
                .put("capabilityInvocation", capabilityInvocation);

        JsonLDObject jsonLdObject = JsonLDObject.fromJson(didDoc.toString());
        JSONObject signedDidDoc = new JSONObject(ldSigner.sign(jsonLdObject).toJson());

    }
}
