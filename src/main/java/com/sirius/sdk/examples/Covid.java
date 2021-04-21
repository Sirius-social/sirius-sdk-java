package com.sirius.sdk.examples;

import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.AttribTranslation;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.messages.ProposedAttrib;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.state_machines.Issuer;
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
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Covid {

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

        public MedSchema setSarsCov2Igm(boolean has) {
            put("sars_cov_2_igm", has);
            return this;
        }

        public MedSchema setSarsCov2Igg(boolean has) {
            put("sars_cov_2_igg", has);
            return this;
        }
    }

    class CredInfo {
        public CredentialDefinition credentialDefinition;
        public Schema schema;
    }

    private CredInfo createMedCreds(Context issuer) {
        Pair<String, String> didVk = issuer.getDid().createAndStoreMyDid();
        String schemaName = "Covid test result";
        Pair<String, AnonCredSchema> schemaPair = issuer.getAnonCreds().issuerCreateSchema(didVk.first, schemaName, "1.0",
                "approved", "timestamp", "bio_location", "location", "full_name", "sars_cov_2_igm", "sars_cov_2_igg");
        AnonCredSchema anoncredSchema = schemaPair.second;
        Ledger ledger = issuer.getLedgers().get("default");
        Pair<Boolean, Schema> okSchema = ledger.registerSchema(anoncredSchema, didVk.first);
        Schema schema = okSchema.second;

        Pair<Boolean, CredentialDefinition> okCredDef = ledger.registerCredDef(new CredentialDefinition("TAG", schema), didVk.first);
        CredentialDefinition credDef = okCredDef.second;

        CredInfo res = new CredInfo();
        res.credentialDefinition = credDef;
        res.schema = schema;
        return res;
    }

    private boolean processMedical(Context context, CredInfo credInfo, MedSchema testResults) throws ExecutionException, InterruptedException {
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

        Listener listener = context.subscribe();
        Event event = listener.getOne().get();
        if (event.getRecipientVerkey().equals(connectionKey) && event.message() instanceof ConnRequest) {
            ConnRequest request = (ConnRequest) event.message();

            Pair<String, String> didVerkey = context.getDid().createAndStoreMyDid();

            Inviter sm = new Inviter(context, new Pairwise.Me(didVerkey.first, didVerkey.second), connectionKey, myEndpoint);
            Pairwise p2p = sm.createConnection(request);
            if (p2p != null) {
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
                boolean ok = issuerMachine.issue(
                        testResults, credInfo.schema, credInfo.credentialDefinition, "Here is your covid test results", "en",
                        new ArrayList<ProposedAttrib>(), translations, credId);
                if (ok) {
                    System.out.println("Covid test confirmation was successfully issued");
                } else {
                    System.out.println("ERROR while issuing");
                }

                return ok;
            }
        }
        return false;
    }

    public static void main(String[] args) {
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
                setApproved("House M.D.").
                setTimestamp(timestamp);

    }
}
