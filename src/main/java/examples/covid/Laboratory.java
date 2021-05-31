package examples.covid;

import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.AttribTranslation;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.ProposedAttrib;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Issuer;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.consensus.simple.messages.ProposeTransactionsMessage;
import com.sirius.sdk.agent.consensus.simple.state_machines.MicroLedgerSimpleConsensus;
import com.sirius.sdk.agent.ledger.CredentialDefinition;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.ledger.Schema;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.agent.microledgers.Transaction;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.Hub;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class Laboratory extends BaseParticipant {

    static List<AttribTranslation> translations = Arrays.asList(
            new AttribTranslation("full_name", "Patient Full Name"),
            new AttribTranslation("location", "Patient location"),
            new AttribTranslation("bio_location", "Biomaterial sampling point"),
            new AttribTranslation("timestamp", "Timestamp"),
            new AttribTranslation("approved", "Laboratory specialist"),
            new AttribTranslation("has_covid", "Covid test result")
    );

    CredInfo medCredInfo;
    Map<String, CovidTest> testResults = new ConcurrentHashMap<>();

    public Laboratory(Hub.Config config, List<Pairwise> pairwises, String covidMicroledgerName, Pairwise.Me me, CredInfo medCredInfo) {
        super(config, pairwises, covidMicroledgerName, me);
        this.medCredInfo = medCredInfo;
    }

    public static CredInfo createMedCreds(Context issuer, String did, String dkmsName) {
        String schemaName = "Covid test result 2";
        Pair<String, AnonCredSchema> schemaPair = issuer.getAnonCreds().issuerCreateSchema(did, schemaName, "1.0",
                "approved", "timestamp", "bio_location", "location", "full_name", "has_covid");
        AnonCredSchema anoncredSchema = schemaPair.second;
        Ledger ledger = issuer.getLedgers().get(dkmsName);

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

    public String issueTestResults(CovidTest testRes) {
        try (Context context = new Context(config)) {
            String connectionKey = context.getCrypto().createKey();
            Endpoint myEndpoint = context.getEndpointWithEmptyRoutingKeys();
            if (myEndpoint == null)
                return null;
            Invitation invitation = Invitation.builder().
                    setLabel("Invitation to connect with medical organization").
                    setRecipientKeys(Collections.singletonList(connectionKey)).
                    setEndpoint(myEndpoint.getAddress()).
                    build();

            String qrContent = invitation.invitationUrl();

            String qrUrl = context.generateQrCode(qrContent);
            if (qrUrl == null)
                return null;

            testResults.put(connectionKey, testRes);
            return qrUrl;
        }
    }

    @Override
    protected void routine() {
        try (Context c = new Context(config)) {
            initMicroledger(c);

            Listener listener = c.subscribe();
            while (loop) {
                Event event = listener.getOne().get();
                if (event.message() instanceof ProposeTransactionsMessage) {
                    processNewCommit(c, event);
                } else if (event.message() instanceof ConnRequest) {
                    processCovidTestRequest(c, event);
                }
            }
        } catch (InterruptedException | ExecutionException ignored) {}
    }

    private void initMicroledger(Context c) {
        if (!c.getMicrolegders().isExists(covidMicroledgerName)) {
            System.out.println("Initializing microledger consensus");
            MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, me);
            Pair<Boolean, AbstractMicroledger> initRes = machine.initMicroledger(covidMicroledgerName, covidMicroledgerParticipants, new ArrayList<>());
            if (initRes.first) {
                System.out.println("Consensus successfully initialized");
            } else {
                System.out.println("Consensus initialization failed!");
            }
        }
    }

    private void processNewCommit(Context c, Event event) {
        MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, event.getPairwise().getMe());
        machine.acceptCommit(event.getPairwise(), (ProposeTransactionsMessage) event.message());
    }

    private void processCovidTestRequest(Context c, Event event) {
        ConnRequest request = (ConnRequest) event.message();
        Pair<String, String> didVerkey = c.getDid().createAndStoreMyDid();
        String connectionKey = event.getRecipientVerkey();
        Endpoint myEndpoint = c.getEndpointWithEmptyRoutingKeys();
        Inviter sm = new Inviter(c, new Pairwise.Me(didVerkey.first, didVerkey.second), connectionKey, myEndpoint);
        Pairwise p2p = sm.createConnection(request);

        Message hello = Message.builder().
                setContext("Welcome to the covid laboratory!").
                setLocale("en").
                build();
        c.sendTo(hello, p2p);

        Issuer issuerMachine = new Issuer(c, p2p, 60);
        String credId = "cred-id-" + UUID.randomUUID().toString();

        List<ProposedAttrib> preview = new ArrayList<>();
        CovidTest testRes = testResults.get(connectionKey);
        for (String key : testRes.keySet()) {
            preview.add(new ProposedAttrib(key, testRes.get(key).toString()));
        }
        boolean ok = issuerMachine.issue(
                testRes, medCredInfo.schema, medCredInfo.credentialDefinition, "Here is your covid test results", "en",
                preview, translations, credId);
        if (ok) {
            System.out.println("Covid test confirmation was successfully issued");
            if (testRes.hasCovid()) {
                AbstractMicroledger ledger = c.getMicrolegders().getLedger(covidMicroledgerName);
                MicroLedgerSimpleConsensus machine = new MicroLedgerSimpleConsensus(c, me);
                Transaction tr = new Transaction(new JSONObject().put("test_res", testRes));
                machine.commit(ledger, covidMicroledgerParticipants, Collections.singletonList(tr));
            }
        } else {
            System.out.println("ERROR while issuing");
        }
    }
}
