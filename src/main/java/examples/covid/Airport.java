package examples.covid;

import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines.Verifier;
import com.sirius.sdk.agent.aries_rfc.feature_0095_basic_message.Message;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.ConnRequest;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.messages.Invitation;
import com.sirius.sdk.agent.aries_rfc.feature_0160_connection_protocol.state_machines.Inviter;
import com.sirius.sdk.agent.connections.Endpoint;
import com.sirius.sdk.agent.consensus.simple.messages.InitRequestLedgerMessage;
import com.sirius.sdk.agent.consensus.simple.messages.ProposeTransactionsMessage;
import com.sirius.sdk.agent.consensus.simple.state_machines.MicroLedgerSimpleConsensus;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.listener.Listener;
import com.sirius.sdk.agent.microledgers.AbstractMicroledger;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.hub.Hub;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Airport extends BaseParticipant {

    CredInfo medCredInfo;
    String labDid;
    CredInfo boardingPassCredInfo;
    String aircompanyDid;
    String dkmsName;

    public Airport(Hub.Config config, CredInfo medCredInfo, String labDid, CredInfo boardingPassCredInfo, String aircompanyDid, String dkmsName) {
        super(config, null, null, null);
        this.medCredInfo = medCredInfo;
        this.labDid = labDid;
        this.boardingPassCredInfo = boardingPassCredInfo;
        this.aircompanyDid = aircompanyDid;
        this.dkmsName = dkmsName;
    }

    @Override
    protected void routine() {
        try (Context c = new Context(config)) {
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
                    ConnRequest request = (ConnRequest) event.message();
                    Pair<String, String> didVerkey = c.getDid().createAndStoreMyDid();
                    String connectionKey = event.getRecipientVerkey();
                    Endpoint myEndpoint = c.getEndpointWithEmptyRoutingKeys();
                    Inviter sm = new Inviter(c, new Pairwise.Me(didVerkey.first, didVerkey.second), connectionKey, myEndpoint);
                    Pairwise pw = sm.createConnection(request);

                    JSONObject proofRequest = (new JSONObject()).
                            put("nonce", c.getAnonCreds().generateNonce()).
                            put("name", "Verify false covid test").
                            put("version", "1.0").
                            put("requested_attributes", (new JSONObject()).
                                    put("attr1_referent", (new JSONObject()).
                                            put("name", "sars_cov_2_igm").
                                            put("restrictions", (new JSONObject()).
                                                    put("issuer_did", labDid))).
                                    put("attr2_referent", (new JSONObject()).
                                            put("name", "flight").
                                            put("restrictions", (new JSONObject()).
                                                    put("issuer_did", aircompanyDid)))
                            );

                    Ledger verLedger = c.getLedgers().get(dkmsName);
                    Verifier machine = new Verifier(c, pw, verLedger);
                    Verifier.VerifyParams params = new Verifier.VerifyParams();
                    params.proofRequest = proofRequest;
                    params.comment = "I am Verifier";
                    params.protoVersion = "1.0";
                    boolean ok = machine.verify(params);
                    if (ok) {
                        System.out.println(machine.getRequestedProof().toString());
                        String val = machine.getRequestedProof().getJSONObject("revealed_attrs").getJSONObject("attr1_referent").optString("raw");
                        if (val.equals("true")) {
                            Message hello = Message.builder().
                                    setContext("Sorry, but we can't let your go to the terminal. Please, get rid of covid first!").
                                    setLocale("en").
                                    build();
                            c.sendTo(hello, pw);
                        } else {
                            Message hello = Message.builder().
                                    setContext("Welcome on board!").
                                    setLocale("en").
                                    build();
                            c.sendTo(hello, pw);
                        }
                    } else {
                        System.out.println("verification failed");
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public String enterToTerminal() {
        try (Context context = new Context(config)) {
            String connectionKey = context.getCrypto().createKey();
            Endpoint myEndpoint = context.getEndpointWithEmptyRoutingKeys();
            if (myEndpoint == null)
                return null;
            Invitation invitation = Invitation.builder().
                    setLabel("Entering to the terminal").
                    setRecipientKeys(Collections.singletonList(connectionKey)).
                    setEndpoint(myEndpoint.getAddress()).
                    build();

            String qrContent = invitation.invitationUrl();

            String qrUrl = context.generateQrCode(qrContent);
            if (qrUrl == null)
                return null;

            return qrUrl;
        }
    }
}
