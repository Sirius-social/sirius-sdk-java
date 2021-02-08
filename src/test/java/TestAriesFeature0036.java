import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.Event;
import com.sirius.sdk.agent.Ledger;
import com.sirius.sdk.agent.Listener;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.*;
import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.hub.Context;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestAriesFeature0036 {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testSane() throws InterruptedException, ExecutionException, TimeoutException {

        Agent issuer = confTest.getAgent("agent1");
        Agent holder = confTest.getAgent("agent2");
        issuer.open();
        holder.open();
        Pairwise i2h = confTest.getPairwise(issuer, holder);
        Pairwise h2i = confTest.getPairwise(holder, issuer);

        String issuerDid = i2h.getMe().getDid();
        String issuerVerkey = i2h.getMe().getVerkey();
        String schemaName = "schema_" + UUID.randomUUID().toString();
        Pair<String, AnonCredSchema> schemaPair = issuer.getWallet().getAnoncreds().issuerCreateSchema(issuerDid, schemaName, "1.0", "attr1", "attr2", "attr3");
        String schemaId = schemaPair.first;
        AnonCredSchema anoncredSchema = schemaPair.second;
        Ledger ledger = issuer.getLedgers().get("default");
        Pair<Boolean, Schema> okSchema = ledger.registerSchema(anoncredSchema, issuerDid);
        Assert.assertTrue(okSchema.first);
        Schema schema = okSchema.second;

        Pair<Boolean, CredentialDefinition> okCredDef = ledger.registerCredDef(new CredentialDefinition("TAG", schema), issuerDid);
        Assert.assertTrue(okCredDef.first);
        CredentialDefinition credDef = okCredDef.second;

        holder.getWallet().getAnoncreds().proverCreateMasterSecret(ConfTest.proverMasterSecretName);

        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams issuerParams = testSuite.getAgentParams("agent1");
        AgentParams holderParams = testSuite.getAgentParams("agent2");
        String holderSecretId = ConfTest.proverMasterSecretName;

        String credId = "cred-id-" + UUID.randomUUID().toString();

        Context context1 = new Context();
        context1.agent = issuer;
        Issuer issuerMachine = new Issuer(context1, i2h, 60);

        Context context2 = new Context();
        context2.agent = holder;
        Holder holderMachine = new Holder(context2, h2i);

        Map<String, String> values = new HashMap<>();
        values.put("attr1", "Value-1");
        values.put("attr2", "567");
        values.put("attr3", "5.7");

        CompletableFuture<Boolean> issuerFuture = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return issuerMachine.issue(
                            values, schema, credDef, "Hello Iam issuer", "en",
                            new ArrayList<ProposedAttrib>(), new ArrayList<AttribTranslation>(), credId);
                });

        CompletableFuture<Pair<Boolean, String>> holderFuture = CompletableFuture.supplyAsync(
                () -> {
                    Event event = null;
                    try {
                        event = context2.agent.subscribe().getOne().get(10, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                        return new Pair<Boolean, String>(false, "");
                    }
                    Message offer = event.message();
                    Assert.assertTrue(offer instanceof OfferCredentialMessage);
                    return holderMachine.accept((OfferCredentialMessage) offer, holderSecretId, "Hello, Iam holder", "en");
                }
        );

        boolean issueRes = issuerFuture.get(10, TimeUnit.SECONDS);
        boolean holderRes = holderFuture.get(10, TimeUnit.SECONDS).first;
    }
}
