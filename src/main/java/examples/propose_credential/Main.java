package examples.propose_credential;

import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.AttribTranslation;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.ProposeCredentialMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.ProposedAttrib;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Issuer;
import com.sirius.sdk.agent.ledger.CredentialDefinition;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.ledger.Schema;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import examples.covid.CredInfo;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main {

    static String serverUri = "https://demo.socialsirius.com";
    static byte[] credentials = "ez8ucxfrTiV1hPX99MHt/C/MUJCo8OmN4AMVmddE/sew8gBzsOg040FWBSXzHd9hDoj5B5KN4aaLiyzTqkrbD3uaeSwmvxVsqkC0xl5dtIc=".getBytes(StandardCharsets.UTF_8);
    static P2PConnection p2PConnection = new P2PConnection("6QvQ3Y5pPMGNgzvs86N3AQo98pF5WrzM1h6WkKH3dL7f",
            "28Au6YoU7oPt6YLpbWkzFryhaQbfAcca9KxZEmz22jJaZoKqABc4UJ9vDjNTtmKSn2Axfu8sT52f5Stmt7JD4zzh",
            "6oczQNLU7bSBzVojkGsfAv3CbXagx7QLUL7Yj1Nba9iw");
    static String publicDid = "Th7MpTaRZVRYnPiabds81Y";

    static final String DKMS_NAME = "test_network";

    public static Pair<String, String> qrCode(Context context) {
        String connectionKey = context.getCrypto().createKey();
        // Теперь сформируем приглашение для других через 0160
        // шаг 1 - определимся какой endpoint мы возьмем, для простоты возьмем endpoint без доп шифрования
        List<Endpoint> endpoints = context.getEndpoints();
        Endpoint myEndpoint = null;
        for (Endpoint e : endpoints) {
            if (e.getRoutingKeys().isEmpty()) {
                myEndpoint = e;
                break;
            }
        }
        if (myEndpoint == null)
            return null;
        // шаг 2 - создаем приглашение
        Invitation invitation = Invitation.builder().
                setLabel("0036 propose-credential test").
                setRecipientKeys(Collections.singletonList(connectionKey)).
                setEndpoint(myEndpoint.getAddress()).
                build();

        // шаг 3 - согласно Aries-0160 генерируем URL
        String qrContent = invitation.invitationUrl();

        // шаг 4 - создаем QR
        String qrUrl = context.generateQrCode(qrContent);
        if (qrUrl == null)
            return null;

        return new Pair<>(connectionKey, qrUrl);
    }

    public static CredInfo regCreds(Context issuer, String did, String dkmsName) {
        String schemaName = "passport";
        Pair<String, AnonCredSchema> schemaPair = issuer.getAnonCreds().issuerCreateSchema(did, schemaName, "1.0",
                "name", "age", "photo");
        AnonCredSchema anoncredSchema = schemaPair.second;
        Ledger ledger = issuer.getLedgers().get(dkmsName);

        Schema schema = ledger.ensureSchemaExists(anoncredSchema, did);

        if (schema == null) {
            Pair<Boolean, Schema> okSchema = ledger.registerSchema(anoncredSchema, did);
            if (okSchema.first) {
                System.out.println("Schema was registered successfully");
                schema = okSchema.second;
            } else {
                System.out.println("Schema was not registered");
                return null;
            }
        } else {
            System.out.println("Schema is already exists in the ledger");
        }

        Pair<Boolean, CredentialDefinition> okCredDef = ledger.registerCredDef(new CredentialDefinition("TAG", schema), did);
        CredentialDefinition credDef = okCredDef.second;

        CredInfo res = new CredInfo();
        res.credentialDefinition = credDef;
        res.schema = schema;
        return res;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (Context context = Context.builder().
                setServerUri(serverUri).
                setCredentials(credentials).
                setP2p(p2PConnection).build()) {
            CredInfo credInfo = regCreds(context, publicDid, DKMS_NAME);

            Pair<String, String> qrCodeRes = qrCode(context);
            String connectionKey = qrCodeRes.first;
            String qrUrl = qrCodeRes.second;
            System.out.println("Открой QR код и просканируй в Sirius App: " + qrUrl);

            Pair<String, String> didVerkey = context.getDid().createAndStoreMyDid();
            String myDid = didVerkey.first;
            String myVerkey = didVerkey.second;

            List<Endpoint> endpoints = context.getEndpoints();
            Endpoint myEndpoint = null;
            for (Endpoint e : endpoints) {
                if (e.getRoutingKeys().isEmpty()) {
                    myEndpoint = e;
                    break;
                }
            }
            if (myEndpoint == null)
                return;
            // Слушаем запросы
            System.out.println("Слушаем запросы");
            Listener listener = context.subscribe();

            Pairwise p2p = null;

            while (true) {
                Event event = listener.getOne().get();

                System.out.println("received: " + event.message().getMessageObj().toString());

                if (event.getRecipientVerkey().equals(connectionKey) && event.message() instanceof ConnRequest) {
                    System.out.println("ConnRequest received");
                    ConnRequest request = (ConnRequest) event.message();

                    Inviter sm = new Inviter(context, new Pairwise.Me(myDid, myVerkey), connectionKey, myEndpoint);
                    p2p = sm.createConnection(request);
                    if (p2p != null) {
                        // Ensure pairwise is stored
                        context.getPairwiseList().ensureExists(p2p);
                        Message hello = Message.builder().
                                setContent("Waiting for your credential propose").
                                setLocale("en").
                                build();
                        context.sendTo(hello, p2p);
                    }
                }

                if (event.getRecipientVerkey().equals(connectionKey) && event.message() instanceof ProposeCredentialMessage) {
                    System.out.println("ProposeCredentialMessage received");
                    if (p2p == null) {
                        System.out.println("Connection not established");
                        return;
                    }
                    ProposeCredentialMessage propose = (ProposeCredentialMessage) event.message();

                    if (!propose.getIssuerDid().equals(publicDid)) {
                        System.out.println("Wrong did");
                    }

                    if (!propose.getCredDefId().equals(credInfo.credentialDefinition.getId())) {
                        System.out.println("Wrong credDefId");
                    }

                    if (!propose.getSchemaId().equals(credInfo.schema.getId())) {
                        System.out.println("Wrong schemaId");
                    }

                    if (!propose.getSchemaIssuerDid().equals(publicDid)) {
                        System.out.println("Wrong schemaIssuerDid");
                    }

                    List<ProposedAttrib> proposedAttribs = propose.getCredentialProposal();
                    JSONObject vals = new JSONObject();
                    for (ProposedAttrib attr : proposedAttribs) {
                        vals.put(attr.optString("name"), attr.get("value"));
                    }

                    Issuer issuerMachine = new Issuer(context, p2p, 60);
                    String credId = "cred-id-" + UUID.randomUUID().toString();

                    boolean ok = issuerMachine.issue(
                            vals, credInfo.schema, credInfo.credentialDefinition, "Here is your passport", "en",
                            proposedAttribs, new ArrayList<AttribTranslation>(), credId);
                    if (ok) {
                        System.out.println("Pasport was successfully issued");
                    } else {
                        System.out.println("ERROR while issuing");
                    }
                }

            }
        }
    }
}
