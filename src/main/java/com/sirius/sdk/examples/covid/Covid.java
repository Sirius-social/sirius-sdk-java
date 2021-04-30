package com.sirius.sdk.examples.covid;

import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.AttribTranslation;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.ProposedAttrib;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Issuer;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines.Verifier;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.consensus.simple.messages.InitRequestLedgerMessage;
import com.sirius.sdk.agent.consensus.simple.messages.ProposeTransactionsMessage;
import com.sirius.sdk.agent.consensus.simple.state_machines.MicroLedgerSimpleConsensus;
import com.sirius.sdk.agent.ledger.CredentialDefinition;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.ledger.Schema;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.agent.model.Entity;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.encryption.P2PConnection;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.Hub;
import com.sirius.sdk.utils.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Covid {

    static final String DKMS_NAME = "test_network";
    static final String COVID_MICROLEDGER_NAME = "covid_ledger_test3";

    static Hub.Config steward = new Hub.Config();
    static Hub.Config laboratory = new Hub.Config();
    static Hub.Config aircompany = new Hub.Config();
    static Hub.Config airport = new Hub.Config();

    static final String LAB_DID = "X1YdguoHBaY1udFQMbbKKG";
    static final String LAB_VERKEY = "HMf57wiWK1FhtzLbm76o37tEMJvaCbWfGsaUzCZVZwnT";
    static final String AIRCOMPANY_DID = "XwVCkzM6sMxk87M2GKtya6";
    static final String AIRPORT_DID = "Ap29nQ3Kf2bGJdWEV3m4AG";

    static final Entity labEntity = new Entity(
            "Laboratory",
            "",
            "HMf57wiWK1FhtzLbm76o37tEMJvaCbWfGsaUzCZVZwnT",
            "X1YdguoHBaY1udFQMbbKKG");

    static final Entity aircompanyEntity = new Entity(
            "AirCompany",
            "",
            "Hs4FPfB1d7nFUcqbMZqofFg4qoeGxGThmSbunJYpVAM6",
            "XwVCkzM6sMxk87M2GKtya6");

    static final Entity airportEntity = new Entity(
            "Airport",
            "",
            "6M8qgMdkqGzQ2yhryV3F9Kvk785qAFny5JuLp1CJCcHW",
            "Ap29nQ3Kf2bGJdWEV3m4AG");

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

        aircompany.serverUri = "https://demo.socialsirius.com";
        aircompany.credentials = "/MYok4BSllG8scfwXVVRK8V47I1PC44mktwiJKKduf38Yb7UgIsq8n4SXVBrRwIzHMQA/6sdiKgrB20Kbw9ieHbOGlxx3UVlWNM0Xfc9Rgk85cCLSHWM2vqlNQSGwHAM+udXpuPwAkfKjiUtzyPBcA==".getBytes(StandardCharsets.UTF_8);
        aircompany.p2p = new P2PConnection(
                "BhDMxfvhc2PZ4BpGTExyWHYkJDFPhmXpaRvUoCoNJ8rL",
                "2wwakvFwBRWbFeLyDbsH6cYVve6FBH6DL133sPNN87jWYbc6rHXj7Q3dnAsbB6EuNwquucsDzSBhNcpxgyVLCCYg",
                "8VNHw79eMTZJBasgjzdwyKyCYA88ajm9gvP98KGcjaBt");

        airport.serverUri = "https://demo.socialsirius.com";
        airport.credentials = "/MYok4BSllG8scfwXVVRK3NATRRtESRnhUHOU3nJxxZ+gg81/srwEPNWfZ+3+6GaEHcqghOJvRoV7taA/vCd2+q2hIEpDO/yCPfMr4x2K0vC/pom1gFRJwJAKI3LpMy3".getBytes(StandardCharsets.UTF_8);
        airport.p2p = new P2PConnection(
                "HBEe9KkPCK4D1zs6UBzLqWp6j2Gj88zy3miqybvYx42p",
                "23jutNJBbgn8bbX53Qr36JSeS2VtZHvY4DMqazXHq6mDEPNkuA3FkKVGAMJdjPznfizLg9nh448DXZ7e1724qk1a",
                "BNxpmTgs9B3yMURa1ta7avKuBA5wcBp5ZmXfqPFPYGAP");
    }

    public static Pairwise establishConnection(Hub.Config myConf, Entity myEntity, Hub.Config theirConf, Entity theirEntity) {
        Context me = new Context(myConf);
        Context their = new Context(theirConf);

        {
            Pairwise pairwise = me.getPairwiseList().loadForDid(theirEntity.getDid());
            boolean isFilled = (pairwise != null) && (pairwise.getMetadata() != null);
            if (!isFilled) {
                Pairwise.Me me_ = new Pairwise.Me(myEntity.getDid(), myEntity.getVerkey());
                Pairwise.Their their_ = new Pairwise.Their(theirEntity.getDid(), theirEntity.getLabel(),
                        their.getEndpointAddressWithEmptyRoutingKeys(), theirEntity.getVerkey(), new ArrayList<>());

                JSONObject metadata = (new JSONObject()).
                        put("me", (new JSONObject()).
                                put("did", myEntity.getDid()).
                                put("verkey", myEntity.getVerkey())).
                        put("their", (new JSONObject()).
                                put("did", theirEntity.getDid()).
                                put("verkey", theirEntity.getVerkey()).
                                put("label", theirEntity.getLabel()).
                                put("endpoint", (new JSONObject()).
                                        put("address", their.getEndpointAddressWithEmptyRoutingKeys()).
                                        put("routing_keys", new JSONArray())));

                pairwise = new Pairwise(me_, their_, metadata);
                me.getDid().storeTheirDid(theirEntity.getDid(), theirEntity.getVerkey());
                me.getPairwiseList().ensureExists(pairwise);
            }
        }

        {
            Pairwise pairwise = their.getPairwiseList().loadForDid(theirEntity.getDid());
            boolean isFilled = (pairwise != null) && (pairwise.getMetadata() != null);
            if (!isFilled) {
                Pairwise.Me me_ = new Pairwise.Me(theirEntity.getDid(), theirEntity.getVerkey());
                Pairwise.Their their_ = new Pairwise.Their(myEntity.getDid(), myEntity.getLabel(),
                        me.getEndpointAddressWithEmptyRoutingKeys(), myEntity.getVerkey(), new ArrayList<>());

                JSONObject metadata = (new JSONObject()).
                        put("me", (new JSONObject()).
                                put("did", theirEntity.getDid()).
                                put("verkey", theirEntity.getVerkey())).
                        put("their", (new JSONObject()).
                                put("did", myEntity.getDid()).
                                put("verkey", myEntity.getVerkey()).
                                put("label", myEntity.getLabel()).
                                put("endpoint", (new JSONObject()).
                                        put("address", me.getEndpointAddressWithEmptyRoutingKeys()).
                                        put("routing_keys", new JSONArray())));

                pairwise = new Pairwise(me_, their_, metadata);
                their.getDid().storeTheirDid(myEntity.getDid(), myEntity.getVerkey());
                their.getPairwiseList().ensureExists(pairwise);
            }
        }

        Pairwise res = me.getPairwiseList().loadForDid(theirEntity.getDid());
        me.close();
        their.close();
        return res;
    }

    static Map<String, MedSchema> testResults = new HashMap<>();
    static Map<String, BoardingPass> boardingPasses = new HashMap<>();
    static CredInfo medCredInfo = null;
    static CredInfo boardingPassCredInfo = null;
    static Map<String/*full_name*/, String/*did*/> aircompanyClientDids = new HashMap<>();

    private static void labRoutine(Pairwise.Me me, String aircompanyDid) {
        try (Context c = new Context(laboratory)) {
            if (!c.getMicrolegders().isExists(COVID_MICROLEDGER_NAME)) {
                System.out.println("Initializing microledger consensus");
                MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, me);
                Pair<Boolean, AbstractMicroledger> initRes = machine.initMicroledger(COVID_MICROLEDGER_NAME, Arrays.asList(me.getDid(), aircompanyDid), new ArrayList<>());
                if (initRes.first) {
                    System.out.println("Consensus successfully initialized");
                } else {
                    System.out.println("Consensus initialization failed!");
                    return;
                }
            }

            Listener listener = c.subscribe();
            while (true) {
                Event event = listener.getOne().get();

                if (event.message() instanceof ProposeTransactionsMessage) {
                    MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, event.getPairwise().getMe());
                    machine.acceptCommit(event.getPairwise(), (ProposeTransactionsMessage) event.message());
                }

                if (event.message() instanceof ConnRequest) {
                    ConnRequest request = (ConnRequest) event.message();
                    Pair<String, String> didVerkey = c.getDid().createAndStoreMyDid();
                    String connectionKey = event.getRecipientVerkey();
                    Endpoint myEndpoint = c.getEndpointWithEmptyRoutingKeys();
                    Inviter sm = new Inviter(c, new Pairwise.Me(didVerkey.first, didVerkey.second), connectionKey, myEndpoint);
                    Pairwise p2p = sm.createConnection(request);

                    Message hello = Message.builder().
                            setContext("Welcome to the covid laboratory!" + (new Date()).toString()).
                            setLocale("en").
                            build();
                    c.sendTo(hello, p2p);

                    Issuer issuerMachine = new Issuer(c, p2p, 60);
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
                    MedSchema testRes = testResults.get(connectionKey);
                    for (String key : testRes.keySet()) {
                        preview.add(new ProposedAttrib(key, testRes.get(key).toString()));
                    }
                    boolean ok = issuerMachine.issue(
                            testRes, medCredInfo.schema, medCredInfo.credentialDefinition, "Here is your covid test results", "en",
                            preview, translations, credId);
                    if (ok) {
                        System.out.println("Covid test confirmation was successfully issued");
                        if (testRes.getSarsCov2Igm() || testRes.getSarsCov2Igg()) {
                            AbstractMicroledger ledger = c.getMicrolegders().getLedger(COVID_MICROLEDGER_NAME);
                            MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, me);
                            Transaction tr = new Transaction(new JSONObject().put("test_res", testRes));
                            machine.commit(ledger, Arrays.asList(LAB_DID, AIRCOMPANY_DID), Arrays.asList(tr));
                        }
                    } else {
                        System.out.println("ERROR while issuing");
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void airportRoutine() {
        try (Context c = new Context(airport)) {
            Listener listener = c.subscribe();
            while (true) {
                Event event = listener.getOne().get();
                if (event.message() instanceof InitRequestLedgerMessage) {
                    MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, event.getPairwise().getMe());
                    Pair<Boolean, AbstractMicroledger> okMl = machine.acceptMicroledger(event.getPairwise(), (InitRequestLedgerMessage) event.message());
                    if (okMl.first) {
                        System.out.println("Microledger for airport created successfully");
                    } else {
                        System.out.println("Microledger for airport creation failed");
                    }
                } else if (event.message() instanceof ProposeTransactionsMessage) {
                    MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, event.getPairwise().getMe());
                    machine.acceptCommit(event.getPairwise(), (ProposeTransactionsMessage) event.message());
                } else if (event.message() instanceof ConnRequest) {

                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void aircompanyRoutine() {
        try (Context c = new Context(aircompany)) {
            Listener listener = c.subscribe();
            while (true) {
                Event event = listener.getOne().get();
                if (event.message() instanceof InitRequestLedgerMessage) {
                    MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, event.getPairwise().getMe());
                    Pair<Boolean, AbstractMicroledger> okMl = machine.acceptMicroledger(event.getPairwise(), (InitRequestLedgerMessage) event.message());
                    if (okMl.first) {
                        System.out.println("Microledger for aircompany created successfully");
                    } else {
                        System.out.println("Microledger for aircompany creation failed");
                    }
                } else if (event.message() instanceof ProposeTransactionsMessage) {
                    ProposeTransactionsMessage propose = (ProposeTransactionsMessage) event.message();
                    MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, event.getPairwise().getMe());
                    machine.acceptCommit(event.getPairwise(), propose);
                    List<Transaction> trs = propose.transactions();
                    for (Transaction tr : trs) {
                        MedSchema testRes = new MedSchema(tr.getJSONObject("test_res"));
                        for (String conn : boardingPasses.keySet()) {
                            BoardingPass pass = boardingPasses.get(conn);
                            if (testRes.getFullName().equals(pass.getFullName())) {
                                Pairwise pw = c.getPairwiseList().loadForDid(aircompanyClientDids.get(pass.getFullName()));
                                Message hello = Message.builder().
                                        setContext("We have to revoke your boarding pass" + (new Date()).toString()).
                                        setLocale("en").
                                        build();
                                c.sendTo(hello, pw);
                            }
                        }
                    }
                } else if (event.message() instanceof ConnRequest) {
                    ConnRequest request = (ConnRequest) event.message();
                    Pair<String, String> didVerkey = c.getDid().createAndStoreMyDid();
                    String connectionKey = event.getRecipientVerkey();
                    Endpoint myEndpoint = c.getEndpointWithEmptyRoutingKeys();
                    Inviter sm = new Inviter(c, new Pairwise.Me(didVerkey.first, didVerkey.second), connectionKey, myEndpoint);
                    Pairwise p2p = sm.createConnection(request);

                    Message hello = Message.builder().
                            setContext("Welcome to the registration!" + (new Date()).toString()).
                            setLocale("en").
                            build();
                    c.sendTo(hello, p2p);

                    Issuer issuerMachine = new Issuer(c, p2p, 60);
                    String credId = "cred-id-" + UUID.randomUUID().toString();
                    List<AttribTranslation> translations = Arrays.asList(
                            new AttribTranslation("full_name", "Full Name"),
                            new AttribTranslation("flight", "Flight num."),
                            new AttribTranslation("departure", "Departure"),
                            new AttribTranslation("arrival", "arrival"),
                            new AttribTranslation("date", "date"),
                            new AttribTranslation("class", "class"),
                            new AttribTranslation("seat", "seat")
                    );
                    List<ProposedAttrib> preview = new ArrayList<ProposedAttrib>();
                    BoardingPass boardingPass = boardingPasses.get(connectionKey);
                    for (String key : boardingPass.keySet()) {
                        preview.add(new ProposedAttrib(key, boardingPass.get(key).toString()));
                    }
                    boolean ok = issuerMachine.issue(
                            boardingPass, boardingPassCredInfo.schema, boardingPassCredInfo.credentialDefinition, "Here is your boarding pass", "en",
                            preview, translations, credId);
                    if (ok) {
                        System.out.println("Boarding pass was successfully issued");
                        c.getPairwiseList().create(p2p);
                        aircompanyClientDids.put(boardingPass.getFullName(), p2p.getTheir().getDid());
                    } else {
                        System.out.println("ERROR while issuing");
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
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

    private static CredInfo createBoardingPassCreds(Context issuer, String did) {
        String schemaName = "Boarding Pass";
        Pair<String, AnonCredSchema> schemaPair = issuer.getAnonCreds().issuerCreateSchema(did, schemaName, "1.0",
                "full_name", "flight", "departure", "arrival", "date", "class", "seat");
        AnonCredSchema anoncredSchema = schemaPair.second;
        Ledger ledger = issuer.getLedgers().get(DKMS_NAME);

        Schema schema = ledger.ensureSchemaExists(anoncredSchema, did);

        if (schema == null) {
            Pair<Boolean, Schema> okSchema = ledger.registerSchema(anoncredSchema, did);
            if (okSchema.first) {
                System.out.println("Boarding pass schema registered successfully");
                schema = okSchema.second;
            } else {
                System.out.println("Boarding pass schema was not registered");
                return null;
            }
        } else {
            System.out.println("Boarding pass schema is exists in the ledger");
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

    private static boolean processMedical(Context context, CredInfo credInfo, MedSchema testResult) throws ExecutionException, InterruptedException {
        String connectionKey = context.getCrypto().createKey();
        Endpoint myEndpoint = context.getEndpointWithEmptyRoutingKeys();
        if (myEndpoint == null)
            return false;
        Invitation invitation = Invitation.builder().
                setLabel("Invitation to connect with medical organization").
                setRecipientKeys(Collections.singletonList(connectionKey)).
                setEndpoint(myEndpoint.getAddress()).
                build();

        String qrContent = invitation.invitationUrl();

        String qrUrl = context.generateQrCode(qrContent);
        if (qrUrl == null)
            return false;

        System.out.println("Scan this QR by Sirius App for receiving the Covid test result " + qrUrl);
        testResults.put(connectionKey, testResult);
        return true;
    }

    private static boolean processAviaRegistration(Context context, BoardingPass boardingPass) throws ExecutionException, InterruptedException {
        String connectionKey = context.getCrypto().createKey();
        Endpoint myEndpoint = context.getEndpointWithEmptyRoutingKeys();
        if (myEndpoint == null)
            return false;
        Invitation invitation = Invitation.builder().
                setLabel("Getting the boarding pass").
                setRecipientKeys(Collections.singletonList(connectionKey)).
                setEndpoint(myEndpoint.getAddress()).
                build();

        String qrContent = invitation.invitationUrl();

        String qrUrl = context.generateQrCode(qrContent);
        if (qrUrl == null)
            return false;

        System.out.println("Scan this QR by Sirius App for receiving boarding pass " + qrUrl);
        boardingPasses.put(connectionKey, boardingPass);
        return true;

//        JSONObject proofRequest = (new JSONObject()).
//                put("nonce", context.getAnonCreds().generateNonce()).
//                put("name", "Verify false covid test").
//                put("version", "1.0").
//                put("requested_attributes", (new JSONObject()).
//                        put("attr1_referent", (new JSONObject()).
//                                put("name", "sars_cov_2_igm").
//                                put("restrictions", (new JSONObject()).
//                                        put("issuer_did", LAB_DID))));
//
//        Ledger verLedger = context.getLedgers().get(DKMS_NAME);
//        Verifier machine = new Verifier(context, v2p, verLedger);
//        Verifier.VerifyParams params = new Verifier.VerifyParams();
//        params.proofRequest = proofRequest;
//        params.comment = "I am Verifier";
//        params.protoVersion = "1.0";
//        boolean ok = machine.verify(params);
//        if (ok) {
//            System.out.println(machine.getRequestedProof().toString());
//            String val = machine.getRequestedProof().getJSONObject("revealed_attrs").getJSONObject("attr1_referent").optString("raw");
//            if (val.equals("true")) {
//                Message hello = Message.builder().
//                        setContext("Sorry, but we can't issue the boarding pass. Please, get rid of covid first!" + (new Date()).toString()).
//                        setLocale("en").
//                        build();
//                context.sendTo(hello, v2p);
//                return false;
//            }
//            Message hello = Message.builder().
//                    setContext("Welcome on board!" + (new Date()).toString()).
//                    setLocale("en").
//                    build();
//            context.sendTo(hello, v2p);
//        } else {
//            System.out.println("verification failed");
//        }
//        return ok;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (Context c = new Context(steward)) {
            if (!c.ping()) {
                System.out.println("Steward agent unreachable");
            }
        }

        try (Context c = new Context(laboratory)) {
            medCredInfo = createMedCreds(c, LAB_DID);
            if (medCredInfo != null) {
                System.out.println("Covid test credentials registered successfully");
            } else {
                System.out.println("Covid test credentials was not registered");
                return;
            }
        }

        try (Context c = new Context(aircompany)) {
            boardingPassCredInfo = createBoardingPassCreds(c, AIRCOMPANY_DID);
            if (boardingPassCredInfo != null) {
                System.out.println("Boarding pass credentials registered successfully");
            } else {
                System.out.println("Boarding pass credentials was not registered");
                return;
            }
        }

        //Pairwise airport2lab = establishConnection(airport, airportEntity, laboratory, labEntity);
        //Pairwise lab2airport = establishConnection(laboratory, labEntity, airport, airportEntity);

        Pairwise lab2aircompany = establishConnection(laboratory, labEntity, aircompany, aircompanyEntity);
        Pairwise aircompany2lab = establishConnection(aircompany, aircompanyEntity, laboratory, labEntity);

        //Pairwise airport2aircompany = establishConnection(airport, airportEntity, aircompany, aircompanyEntity);
        //Pairwise aircompany2airport = establishConnection(aircompany, aircompanyEntity, airport, airportEntity);


        new Thread(() -> labRoutine(lab2aircompany.getMe(), lab2aircompany.getTheir().getDid())).start();
        //new Thread(() -> airportRoutine()).start();
        new Thread(() -> aircompanyRoutine()).start();

        Scanner in = new Scanner(System.in);
        System.out.println("Enter your Name");
        String fullName = in.nextLine();

        boolean loop = true;
        while (loop) {
            System.out.println("Enter your option:");
            System.out.println("1 - Get Covid test");
            System.out.println("2 - Get boarding pass");
            System.out.println("3 - Enter to the terminal");
            System.out.println("4 - Exit");

            int option = in.nextInt();
            switch (option) {
                case 1: {
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
                }
                break;
                case 2: {
                    try (Context c = new Context(aircompany)) {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        String timestamp = df.format(new Date(System.currentTimeMillis()));
                        BoardingPass boardingPass = new BoardingPass().
                                setFullName(fullName).
                                setArrival("Nur-Sultan").
                                setDeparture("New York JFK").
                                setClass("first").
                                setDate(timestamp).
                                setFlight("KC 1234").
                                setSeat("1A");
                        processAviaRegistration(c, boardingPass);
                    }
                }
                break;
                case 3: {

                } break;
                case 4: {
                    loop = false;
                } break;
            }
        }
    }
}
