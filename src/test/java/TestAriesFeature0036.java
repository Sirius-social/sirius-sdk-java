import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.Ledger;
import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import models.AgentParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class TestAriesFeature0036 {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testSane() {

        Agent issuer = null;
        Agent holder = null;
        try {
            issuer = confTest.getAgent("agent1");
            holder = confTest.getAgent("agent2");
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

            Pair<Boolean, CredentialDefinition> okCredDef = ledger.registerCredDef(new CredentialDefinition("tag", schema), issuerDid);
            Assert.assertTrue(okCredDef.first);

            holder.getWallet().getAnoncreds().proverCreateMasterSecret(ConfTest.proverMasterSecretName);
        } finally {
            issuer.close();
            holder.close();
        }

        ServerTestSuite testSuite = confTest.getSuiteSingleton();
        AgentParams issuerParams = testSuite.getAgentParams("agent1");
        AgentParams holderParams = testSuite.getAgentParams("agent2");
        String holderSecretId = ConfTest.proverMasterSecretName;

        String credId = "cred-id-" + UUID.randomUUID().toString();



    }
}
