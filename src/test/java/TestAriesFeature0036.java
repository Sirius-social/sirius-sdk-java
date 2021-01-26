import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.Ledger;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.AttribTranslation;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.Issuer;
import com.sirius.sdk.agent.aries_rfc.feature_0036_issue_credential.ProposedAttrib;
import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.model.pairwise.Pairwise;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.hub.Context;
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

public class TestAriesFeature0036 {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testSane() {

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

        Context context = new Context();
        context.agent = issuer;
        Issuer issuerMachine = new Issuer(context, i2h, 60);

        Map<String, String> values = new HashMap<>();
        values.put("attr1", "Value-1");
        values.put("attr2", "567");
        values.put("attr3", "5.7");
        issuerMachine.issue(values, schema, credDef, "Hello Iam issuer", "en", new ArrayList<ProposedAttrib>(),
                new ArrayList<AttribTranslation>(), credId);

    }
}
