package examples.covid;

import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.AttribTranslation;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.ProposedAttrib;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Issuer;
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
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.Hub;
import com.sirius.sdk.utils.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class AirCompany extends BaseParticipant {

    static List<AttribTranslation> translations = Arrays.asList(
            new AttribTranslation("full_name", "Full Name"),
            new AttribTranslation("flight", "Flight num."),
            new AttribTranslation("departure", "Departure"),
            new AttribTranslation("arrival", "arrival"),
            new AttribTranslation("date", "date"),
            new AttribTranslation("class", "class"),
            new AttribTranslation("seat", "seat")
    );

    CredInfo boardingPassCredInfo = null;
    Map<String, BoardingPass> boardingPasses = new ConcurrentHashMap<>();
    Map<String/*full_name*/, String/*did*/> aircompanyClientDids = new HashMap<>();

    public AirCompany(Hub.Config config, List<Pairwise> pairwises, String covidMicroledgerName, Pairwise.Me me, CredInfo boardingPassCredInfo) {
        super(config, pairwises, covidMicroledgerName, me);
        this.boardingPassCredInfo = boardingPassCredInfo;
    }

    public static CredInfo createBoardingPassCreds(Context issuer, String did, String dkmsName) {
        String schemaName = "Boarding Pass";
        Pair<String, AnonCredSchema> schemaPair = issuer.getAnonCreds().issuerCreateSchema(did, schemaName, "1.0",
                "full_name", "flight", "departure", "arrival", "date", "class", "seat");
        AnonCredSchema anoncredSchema = schemaPair.second;
        Ledger ledger = issuer.getLedgers().get(dkmsName);

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

    @Override
    protected void routine() {
        try (Context c = new Context(config)) {
            Listener listener = c.subscribe();
            while (loop) {
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
                            setContext("Welcome to the registration!").
                            setLocale("en").
                            build();
                    c.sendTo(hello, p2p);

                    Issuer issuerMachine = new Issuer(c, p2p, 60);
                    String credId = "cred-id-" + UUID.randomUUID().toString();

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

    public String register(BoardingPass boardingPass) {
        try (Context context = new Context(config)) {
            String connectionKey = context.getCrypto().createKey();
            Endpoint myEndpoint = context.getEndpointWithEmptyRoutingKeys();
            if (myEndpoint == null)
                return null;
            Invitation invitation = Invitation.builder().
                    setLabel("Getting the boarding pass").
                    setRecipientKeys(Collections.singletonList(connectionKey)).
                    setEndpoint(myEndpoint.getAddress()).
                    build();

            String qrContent = invitation.invitationUrl();

            String qrUrl = context.generateQrCode(qrContent);
            if (qrUrl == null)
                return null;

            boardingPasses.put(connectionKey, boardingPass);
            return qrUrl;
        }
    }
}
