import com.sirius.sdk.agent.CloudAgent;
import com.sirius.sdk.agent.Codec;
import com.sirius.sdk.agent.listener.Event;
import com.sirius.sdk.agent.ledger.Ledger;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.messages.RequestPresentationMessage;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines.Prover;
import com.sirius.sdk.agent.aries_rfc.feature_0037_present_proof.state_machines.Verifier;
import com.sirius.sdk.agent.ledger.CredentialDefinition;
import com.sirius.sdk.agent.ledger.Schema;
import com.sirius.sdk.agent.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.errors.indy_exceptions.DuplicateMasterSecretNameException;
import com.sirius.sdk.hub.CloudContext;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.Triple;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class TestAriesFeature0037 {

    ConfTest confTest;
    Logger log = Logger.getLogger(TestAriesFeature0037.class.getName());

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testSane() throws InterruptedException, ExecutionException, TimeoutException {
        CloudAgent issuer = confTest.getAgent("agent1");
        CloudAgent prover = confTest.getAgent("agent2");
        CloudAgent verifier = confTest.getAgent("agent3");

        issuer.open();
        prover.open();
        verifier.open();

        log.info("Establish pairwises");
        Pairwise i2p = confTest.getPairwise(issuer, prover);
        Pairwise p2i = confTest.getPairwise(prover, issuer);
        Pairwise v2p = confTest.getPairwise(verifier, prover);
        Pairwise p2v = confTest.getPairwise(prover, verifier);

        log.info("Register schema");
        String issuerDid = i2p.getMe().getDid();
        String issuerVerkey = i2p.getMe().getVerkey();
        String schemaName = "schema_" + UUID.randomUUID().toString();
        Pair<String, AnonCredSchema> schemaPair = issuer.getWallet().getAnoncreds().issuerCreateSchema(issuerDid, schemaName, "1.0", "attr1", "attr2", "attr3");
        String schemaId = schemaPair.first;
        AnonCredSchema anoncredSchema = schemaPair.second;
        Ledger ledger = issuer.getLedgers().get("default");
        Pair<Boolean, Schema> okSchema = ledger.registerSchema(anoncredSchema, issuerDid);
        Assert.assertTrue(okSchema.first);
        Schema schema = okSchema.second;

        log.info("Register credential def");
        Pair<Boolean, CredentialDefinition> okCredDef = ledger.registerCredDef(new CredentialDefinition("TAG", schema), issuerDid);
        Assert.assertTrue(okCredDef.first);
        CredentialDefinition credDef = okCredDef.second;

        log.info("Prepare Prover");
        try {
            prover.getWallet().getAnoncreds().proverCreateMasterSecret(ConfTest.proverMasterSecretName);
        } catch (DuplicateMasterSecretNameException ignored) {}

        String proverSecretId = ConfTest.proverMasterSecretName;
        JSONObject credValues = (new JSONObject()).
                put("attr1", "Value-1").
                put("attr2", 456).
                put("attr3", 4.67);

        String credId = "cred-id-" + UUID.randomUUID().toString();

        // Issue credential
        JSONObject offer = issuer.getWallet().getAnoncreds().issuerCreateCredentialOffer(credDef.getId());
        Pair<JSONObject, JSONObject> proverCreateCredentialReqRes = prover.getWallet().getAnoncreds().proverCreateCredentialReq(p2i.getMe().getDid(), offer, new JSONObject(credDef.getBody().toString()), proverSecretId);
        JSONObject credRequest = proverCreateCredentialReqRes.first;
        JSONObject credMetadata = proverCreateCredentialReqRes.second;

        JSONObject encodedCredValues = new JSONObject();
        for (String key : credValues.keySet()) {
            JSONObject encCredVal = new JSONObject();
            encCredVal.put("raw", credValues.get(key).toString());
            encCredVal.put("encoded", Codec.encode(credValues.get(key)));
            encodedCredValues.put(key, encCredVal);
        }
        Triple<JSONObject, String, JSONObject> issuerCreateCredentialRes = issuer.getWallet().getAnoncreds().issuerCreateCredential(offer, credRequest, encodedCredValues);
        JSONObject cred = issuerCreateCredentialRes.first;
        String credRevocId = issuerCreateCredentialRes.second;
        JSONObject revocRegDelta = issuerCreateCredentialRes.third;

        prover.getWallet().getAnoncreds().proverStoreCredential(credId, credMetadata, cred, new JSONObject(credDef.getBody().toString()));

        issuer.close();
        prover.close();
        verifier.close();

        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams proverParams = testSuite.getAgentParams("agent2");
        AgentParams verifierParams = testSuite.getAgentParams("agent3");

        String attrReferentId = "attr1_referent";
        String predReferentId = "predicate1_referent";

        JSONObject proofRequest = null;
        try (Context context = CloudContext.builder().
                setServerUri(verifierParams.getServerAddress()).
                setCredentials(verifierParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
                setP2p(verifierParams.getConnection()).
                build()) {
            proofRequest = (new JSONObject()).
                    put("nonce", context.getAnonCreds().generateNonce()).
                    put("name", "Test ProofRequest").
                    put("version", "0.1").
                    put("requested_attributes", (new JSONObject()).
                            put(attrReferentId, (new JSONObject()).
                                    put("name", "attr1").
                                    put("restrictions", (new JSONObject()).
                                            put("issuer_did", issuerDid)))).
                    put("requested_predicates", (new JSONObject()).
                            put(predReferentId, (new JSONObject()).
                                    put("name", "attr2").
                                    put("p_type", ">=").
                                    put("p_value", 100).
                                    put("restrictions", (new JSONObject()).
                                            put("issuer_did", issuerDid))));
        }
        //run_verifier
        JSONObject finalProofRequest = proofRequest;
        CompletableFuture<Boolean> runVerifier = CompletableFuture.supplyAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(verifierParams.getServerAddress()).
                    setCredentials(verifierParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
                    setP2p(verifierParams.getConnection()).
                    setTimeoutSec(60).
                    build()) {
                Ledger verLedger = context.getLedgers().get("default");
                Verifier machine = new Verifier(context, v2p, verLedger);
                return machine.verify(new Verifier.VerifyParams().
                        setProofRequest(finalProofRequest).
                        setComment("I am Verifier").
                        setProtocolVersion("1.0"));
            }
        }, r -> new Thread(r).start());

        //run prover
        CompletableFuture<Boolean> runProver = CompletableFuture.supplyAsync(() -> {
            try (Context context = CloudContext.builder().
                    setServerUri(proverParams.getServerAddress()).
                    setCredentials(proverParams.getCredentials().getBytes(StandardCharsets.UTF_8)).
                    setP2p(proverParams.getConnection()).
                    setTimeoutSec(60).
                    build()) {
                Event event = null;
                try {
                    event = context.subscribe().getOne().get(30, TimeUnit.SECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                    return false;
                }
                Assert.assertTrue(event.message() instanceof RequestPresentationMessage);
                RequestPresentationMessage requestPresentationMessage = (RequestPresentationMessage) event.message();
                int ttl = 60;
                Ledger proverLedger = context.getLedgers().get("default");
                Prover machine = new Prover(context, p2v, proverLedger, proverSecretId);
                return machine.prove(requestPresentationMessage);
            }
        }, r -> new Thread(r).start());

        Assert.assertTrue(runProver.get(60, TimeUnit.SECONDS));
        Assert.assertTrue(runVerifier.get(60, TimeUnit.SECONDS));
    }
}