package com.sirius.sdk.examples;

import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.AttribTranslation;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.ProposedAttrib;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Issuer;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines.Verifier;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.ledger.CredentialDefinition;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.ledger.Schema;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.Hub;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Covid {

    static final String DKMS_NAME = "test_network";

    static Hub.Config steward = new Hub.Config();
    static Hub.Config laboratory = new Hub.Config();
    static Hub.Config airport = new Hub.Config();


    static final String LAB_DID = "X1YdguoHBaY1udFQMbbKKG";

    static {
        steward.serverUri = "https://demo.socialsirius.com";
        steward.credentials = "ez8ucxfrTiV1hPX99MHt/C/MUJCo8OmN4AMVmddE/sew8gBzsOg040FWBSXzHd9hDoj5B5KN4aaLiyzTqkrbD3uaeSwmvxVsqkC0xl5dtIc=".getBytes(StandardCharsets.UTF_8);
        steward.p2p = new P2PConnection(
                "6QvQ3Y5pPMGNgzvs86N3AQo98pF5WrzM1h6WkKH3dL7f",
                "28Au6YoU7oPt6YLpbWkzFryhaQbfAcca9KxZEmz22jJaZoKqABc4UJ9vDjNTtmKSn2Axfu8sT52f5Stmt7JD4zzh",
                "6oczQNLU7bSBzVojkGsfAv3CbXagx7QLUL7Yj1Nba9iw");

        laboratory.serverUri = "https://demo.socialsirius.com";
        laboratory.credentials = "BXXwMmUlw7MTtVWhcVvbSVWbC1GopGXDuo+oY3jHkP/4jN3eTlPDwSwJATJbzwuPAAaULe6HFEP5V57H6HWNqYL4YtzWCkW2w+H7fLgrfTLaBtnD7/P6c5TDbBvGucOV".getBytes(StandardCharsets.UTF_8);
        laboratory.p2p = new P2PConnection(
                "EzJKT2Q6Cw8pwy34xPa9m2qPCSvrMmCutaq1pPGBQNCn",
                "273BEpAM8chzfMBDSZXKhRMPPoaPRWRDtdMmNoKLmJUU6jvm8Nu8caa7dEdcsvKpCTHmipieSsatR4aMb1E8hQAa",
                "342Bm3Eq9ruYfvHVtLxiBLLFj54Tq6p8Msggt7HiWxBt");

        airport.serverUri = "https://demo.socialsirius.com";
        airport.credentials = "/MYok4BSllG8scfwXVVRK3NATRRtESRnhUHOU3nJxxZ+gg81/srwEPNWfZ+3+6GaEHcqghOJvRoV7taA/vCd2+q2hIEpDO/yCPfMr4x2K0vC/pom1gFRJwJAKI3LpMy3".getBytes(StandardCharsets.UTF_8);
        airport.p2p = new P2PConnection(
                "HBEe9KkPCK4D1zs6UBzLqWp6j2Gj88zy3miqybvYx42p",
                "23jutNJBbgn8bbX53Qr36JSeS2VtZHvY4DMqazXHq6mDEPNkuA3FkKVGAMJdjPznfizLg9nh448DXZ7e1724qk1a",
                "BNxpmTgs9B3yMURa1ta7avKuBA5wcBp5ZmXfqPFPYGAP");
    }

    static class MedSchema extends JSONObject {

        public MedSchema() {
            super();
        }

        public MedSchema setFullName(String name) {
            put("full_name", name);
            return this;
        }

        public MedSchema setLocation(String location) {
            put("location", location);
            return this;
        }

        public MedSchema setBioLocation(String bioLocation) {
            put("bio_location", bioLocation);
            return this;
        }

        public MedSchema setTimestamp(String timestamp) {
            put("timestamp", timestamp);
            return this;
        }

        public MedSchema setApproved(String approved) {
            put("approved", approved);
            return this;
        }

        public MedSchema setSarsCov2Igm(Boolean has) {
            put("sars_cov_2_igm", has.toString());
            return this;
        }

        public MedSchema setSarsCov2Igg(Boolean has) {
            put("sars_cov_2_igg", has.toString());
            return this;
        }
    }

    static class BoardingPass extends JSONObject {

        public BoardingPass() {
            super();
        }

        public BoardingPass setFlight(String flight) {
            put("flight", flight);
            return this;
        }

        public BoardingPass setDeparture(String departure) {
            put("departure", departure);
            return this;
        }

        public BoardingPass setArrival(String arrival) {
            put("arrival", arrival);
            return this;
        }

        public BoardingPass setDate(String date) {
            put("date", date);
            return this;
        }

        public BoardingPass setClass(String cls) {
            put("class", cls);
            return this;
        }

        public BoardingPass setSeat(String seat) {
            put("seat", seat);
            return this;
        }
    }

    static class CredInfo {
        public CredentialDefinition credentialDefinition;
        public Schema schema;
    }

    private static CredInfo createMedCreds(Context issuer, String did) {
        String schemaName = "Covid test result";
        Pair<String, AnonCredSchema> schemaPair = issuer.getAnonCreds().issuerCreateSchema(did, schemaName, "1.0",
                "approved", "timestamp", "bio_location", "location", "full_name", "sars_cov_2_igm", "sars_cov_2_igg");
        AnonCredSchema anoncredSchema = schemaPair.second;
        Ledger ledger = issuer.getLedgers().get(DKMS_NAME);

        Schema schema = ledger.ensureSchemaExists(anoncredSchema, did);

        if (schema == null) {
            Pair<Boolean, Schema> okSchema = ledger.registerSchema(anoncredSchema, did);
            if (okSchema.first) {
                System.out.println("Covid test result registered successfully");
                schema = okSchema.second;
            } else {
                System.out.println("Covid test result was not registered");
                return null;
            }
        } else {
            System.out.println("Med schema is exists in the ledger");
        }

        Pair<Boolean, CredentialDefinition> okCredDef = ledger.registerCredDef(new CredentialDefinition("TAG", schema), did);
        CredentialDefinition credDef = okCredDef.second;

        CredInfo res = new CredInfo();
        res.credentialDefinition = credDef;
        res.schema = schema;
        return res;
    }

    private static Pairwise establishConnectionByQr(Context context, String inviteLabel) {
        String connectionKey = context.getCrypto().createKey();
        Endpoint myEndpoint = context.getEndpointWithEmptyRoutingKeys();
        if (myEndpoint == null)
            return null;
        Invitation invitation = Invitation.builder().
                setLabel(inviteLabel).
                setRecipientKeys(Collections.singletonList(connectionKey)).
                setEndpoint(myEndpoint.getAddress()).
                build();

        String qrContent = invitation.invitationUrl();

        String qrUrl = context.generateQrCode(qrContent);
        if (qrUrl == null)
            return null;

        System.out.println("Scan this QR by Sirius App for receiving the Covid test result " + qrUrl);

        Listener listener = context.subscribe();
        Event event = null;
        try {
            event = listener.getOne().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
        if (event.getRecipientVerkey().equals(connectionKey) && event.message() instanceof ConnRequest) {
            ConnRequest request = (ConnRequest) event.message();

            Pair<String, String> didVerkey = context.getDid().createAndStoreMyDid();

            Inviter sm = new Inviter(context, new Pairwise.Me(didVerkey.first, didVerkey.second), connectionKey, myEndpoint);
            Pairwise p2p = sm.createConnection(request);
            return p2p;
        }
        return null;
    }

    private static boolean processMedical(Context context, CredInfo credInfo, MedSchema testResults) throws ExecutionException, InterruptedException {
        Pairwise p2p = establishConnectionByQr(context, "Invitation to connect with medical organization");
        if (p2p != null) {

            Message hello = Message.builder().
                    setContext("Hello!!!" + (new Date()).toString()).
                    setLocale("en").
                    build();
            context.sendTo(hello, p2p);

            Issuer issuerMachine = new Issuer(context, p2p, 60);
            String credId = "cred-id-" + UUID.randomUUID().toString();
            List<AttribTranslation> translations = Arrays.asList(
                    new AttribTranslation("full_name", "Patient Full Name"),
                    new AttribTranslation("location", "Patient location"),
                    new AttribTranslation("bio_location", "Biomaterial sampling point"),
                    new AttribTranslation("timestamp", "Timestamp"),
                    new AttribTranslation("approved", "Laboratory specialist"),
                    new AttribTranslation("sars_cov_2_igm", "SARS-CoV-2 IgM"),
                    new AttribTranslation("sars_cov_2_igg", "SARS-CoV-2 IgG")
            );
            List<ProposedAttrib> preview = new ArrayList<ProposedAttrib>();
            for (String key : testResults.keySet()) {
                preview.add(new ProposedAttrib(key, testResults.get(key).toString()));
            }
            boolean ok = issuerMachine.issue(
                    testResults, credInfo.schema, credInfo.credentialDefinition, "Here is your covid test results", "en",
                    preview, translations, credId);
            if (ok) {
                System.out.println("Covid test confirmation was successfully issued");
            } else {
                System.out.println("ERROR while issuing");
            }

            return ok;
        }
        return false;
    }

    private static boolean processAviaRegistration(Context context) throws ExecutionException, InterruptedException {
        Pairwise v2p = establishConnectionByQr(context, "Connect with airport");
        if (v2p == null)
            return false;

        JSONObject proofRequest = (new JSONObject()).
                put("nonce", context.getAnonCreds().generateNonce()).
                put("name", "Verify false covid test").
                put("version", "1.0").
                put("requested_attributes", (new JSONObject()).
                        put("attr1_referent", (new JSONObject()).
                                put("name", "sars_cov_2_igm").
                                put("restrictions", (new JSONObject()).
                                        put("issuer_did", LAB_DID))));

        Ledger verLedger = context.getLedgers().get(DKMS_NAME);
        Verifier machine = new Verifier(context, v2p, verLedger);
        Verifier.VerifyParams params = new Verifier.VerifyParams();
        params.proofRequest = proofRequest;
        params.comment = "I am Verifier";
        params.protoVersion = "1.0";
        boolean ok = machine.verify(params);
        if (ok) {
            System.out.println(machine.getRequestedProof().toString());
        } else {
            System.out.println("verification failed");
        }
        return ok;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (Context c = new Context(steward)) {
            if (!c.ping()) {
                System.out.println("Steward agent unreachable");
            }
        }

        CredInfo medCredInfo = null;
        try (Context c = new Context(laboratory)) {
            medCredInfo = createMedCreds(c, LAB_DID);
            if (medCredInfo != null) {
                System.out.println("Covid test credentials registered successfully");
            } else {
                System.out.println("Covid test credentials was not registered");
                return;
            }
        }


        Scanner in = new Scanner(System.in);
        System.out.println("Enter your Name");
        String fullName = in.nextLine();
        System.out.println("Do you have Covid? (true/false)");
        boolean hasCovid = in.nextBoolean();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String timestamp = df.format(new Date(System.currentTimeMillis()));
        MedSchema testRes = new MedSchema().
                setFullName(fullName).
                setSarsCov2Igg(hasCovid).
                setSarsCov2Igm(hasCovid).
                setLocation("Nur-Sultan").
                setBioLocation("Nur-Sultan").
                setApproved("House M.D.").
                setTimestamp(timestamp);

        try (Context c = new Context(laboratory)) {
            processMedical(c, medCredInfo, testRes);
        }

        try (Context c = new Context(airport)) {
            processAviaRegistration(c);
        }

    }
}
