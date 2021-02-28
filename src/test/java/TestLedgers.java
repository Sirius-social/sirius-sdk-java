import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.Ledger;
import com.sirius.sdk.agent.model.ledger.CredentialDefinition;
import com.sirius.sdk.agent.model.ledger.Schema;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class TestLedgers {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }


    @Test
    public void testSchemaRegistration() {
        Agent agent1 = confTest.agent1();
        agent1.open();
        String seed = "000000000000000000000000Steward1";
        Pair<String, String> didVerkey = agent1.getWallet().getDid().createAndStoreMyDid(null, seed);

        String schemaName = "schema_" + UUID.randomUUID().getLeastSignificantBits();
        Pair<String, AnonCredSchema> schemaIdAnonCredSchema = agent1.getWallet().getAnoncreds().issuerCreateSchema(didVerkey.first, schemaName, "1.0"
                , "attr1", "attr2", "attr3");
        Ledger ledger = agent1.getLedgers().get("default");
        Pair<Boolean, Schema> okSchema = ledger.registerSchema(schemaIdAnonCredSchema.second, didVerkey.first);

        Assert.assertTrue(okSchema.first);
        Assert.assertTrue(okSchema.second.getSeqNo() > 0);

        Pair<Boolean, Schema> okSchema2 = ledger.registerSchema(schemaIdAnonCredSchema.second, didVerkey.first);

        Assert.assertFalse(okSchema2.first);

        Schema restoredSchema = ledger.ensureSchemaExists(schemaIdAnonCredSchema.second, didVerkey.first);
        Assert.assertNotNull(restoredSchema);
        Assert.assertEquals(okSchema.second.serialize(), restoredSchema.serialize());

        agent1.close();
    }

    @Test
    public void testSchemaLoading(){
        Agent agent1 = confTest.agent1();
        Agent agent2 = confTest.agent2();
        agent1.open();
        agent2.open();
        String seed1 = "000000000000000000000000Steward1";

        Pair<String,String> didVerkey1 = agent1.getWallet().getDid().createAndStoreMyDid(null, seed1);
        String schemaName = "schema_" + UUID.randomUUID().getMostSignificantBits();
      Pair<String,AnonCredSchema> didAnoncredSchema =   agent1.getWallet().getAnoncreds().issuerCreateSchema(didVerkey1.first,schemaName,"1.0","attr1", "attr2", "attr3");
        Ledger ledger1 = agent1.getLedgers().get("default");
       Pair<Boolean,Schema> okSchema =  ledger1.registerSchema(didAnoncredSchema.second,didVerkey1.first);

       Assert.assertTrue(okSchema.first);
       Assert.assertTrue(okSchema.second.getSeqNo() > 0);

       String seed2 = "000000000000000000000000Trustee0";
        Pair<String,String> didVerkey2 = agent2.getWallet().getDid().createAndStoreMyDid(null, seed2);
        Ledger ledger2 = agent2.getLedgers().get("default");
        for (int i=0;i<5;i++){
           Schema laodedSchema =  ledger2.loadSchema(okSchema.second.getId(),didVerkey2.first);
           Assert.assertNotNull(laodedSchema);
           Assert.assertEquals(okSchema.second.serializeToJsonObject().toString(),laodedSchema.serializeToJsonObject().toString());
        }
        agent1.close();
        agent2.close();

    }

    @Test
    public void testSchemaFetching(){
        Agent agent1 = confTest.agent1();
        agent1.open();
        String seed = "000000000000000000000000Steward1";
        Pair<String,String> didVerkey = agent1.getWallet().getDid().createAndStoreMyDid(null,seed);

        String schemaName = "schema_" + UUID.randomUUID().getMostSignificantBits();

        Pair<String,AnonCredSchema> didSchema = agent1.getWallet().getAnoncreds().
                issuerCreateSchema(didVerkey.first,schemaName,"1.0","attr1", "attr2", "attr3");

        Ledger ledger = agent1.getLedgers().get("default");
      Pair<Boolean,Schema> okSchema =   ledger.registerSchema(didSchema.second,didVerkey.first);

      Assert.assertTrue(okSchema.first);

        List<Schema> fetches =  ledger.fetchSchemas(null,schemaName);
        Assert.assertEquals(1, fetches.size());
        Assert.assertEquals(didVerkey.first,fetches.get(0).getIssuerDid());


        agent1.close();
    }



    @Test
    public void testRegisterCredDef(){
        Agent agent1 = confTest.agent1();
        agent1.open();
        String seed = "000000000000000000000000Steward1";
        Pair<String,String> didVerkey = agent1.getWallet().getDid().createAndStoreMyDid(null,seed);
        String schemaName = "schema_" + UUID.randomUUID().getMostSignificantBits();

        Pair<String,AnonCredSchema> didSchema = agent1.getWallet().getAnoncreds().
                issuerCreateSchema(didVerkey.first,schemaName,"1.0","attr1", "attr2", "attr3");
        Ledger ledger = agent1.getLedgers().get("default");
        Pair<Boolean,Schema> okSchema =ledger.registerSchema(didSchema.second,didVerkey.first);
        Assert.assertTrue(okSchema.first);

        CredentialDefinition credDef = new CredentialDefinition("Test Tag",okSchema.second);
        Assert.assertNull(  credDef.getBody());

    
        agent1.close();
    }

   /*

            ok, ledger_cred_def = await ledger.register_cred_def(cred_def=cred_def, submitter_did=did)
            assert ok is True
        assert ledger_cred_def.body is not None
        assert ledger_cred_def.seq_no > 0
            assert ledger_cred_def.submitter_did == did
            my_value = 'my-value-' + uuid.uuid4().hex

    ok, ledger_cred_def2 = await ledger.register_cred_def(
            cred_def=cred_def, submitter_did=did, tags={'my_tag': my_value}
        )
                assert ok is True
        assert ledger_cred_def.body == ledger_cred_def2.body
        assert ledger_cred_def2.seq_no > ledger_cred_def.seq_no

            ser = ledger_cred_def.serialize()
    loaded = CredentialDefinition.deserialize(ser)
            assert loaded.body == ledger_cred_def.body
        assert loaded.seq_no == ledger_cred_def.seq_no
        assert loaded.schema.body == ledger_cred_def.schema.body
        assert loaded.config.serialize() == ledger_cred_def.config.serialize()

    results = await ledger.fetch_cred_defs(schema_id=schema_id)
        assert len(results) == 2
    results = await ledger.fetch_cred_defs(my_tag=my_value)
        assert len(results) == 1

    parts = ledger_cred_def.id.split(':')
    print(str(parts))

    opts = CacheOptions()
        for n in range(3):
    cached_body = await agent1.wallet.cache.get_cred_def('default', did, ledger_cred_def.id, opts)
            assert cached_body == ledger_cred_def.body
            cred_def = await ledger.load_cred_def(ledger_cred_def.id, did)
            assert cred_def.body == cached_body
    finally:
    await agent1.close()*/


}
