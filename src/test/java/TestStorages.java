import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.InWalletImmutableCollection;
import com.sirius.sdk.storage.impl.InMemoryImmutableCollection;
import com.sirius.sdk.storage.impl.InMemoryKeyValueStorage;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

public class TestStorages {

    @Test
    public void testInMemoryKvStorage() {
        InMemoryKeyValueStorage kv = new InMemoryKeyValueStorage();
        kv.selectDb("db1");
        kv.set("key1", "value1");
        Object value = kv.get("key1");
        Assert.assertEquals("value1", value);

        kv.selectDb("db2");
        kv.set("key1", 1000);
        value = kv.get("key1");
        Assert.assertEquals(1000, value);

        kv.selectDb("db1");
        value = kv.get("key1");
        Assert.assertEquals("value1", value);

        kv.delete("key1");
        value = kv.get("key1");
        Assert.assertNull(value);

        kv.delete("unknown-key");
    }

    @Test
    public void testInMemoryImmutableCollection() {
        InMemoryImmutableCollection collection = new InMemoryImmutableCollection();
        collection.selectDb("db1");
        collection.add("Value1","{\"tag1\": \"tag-val-1\", \"tag2\": \"tag-val-2\"}");
        collection.add("Value2","{\"tag1\": \"tag-val-1\", \"tag2\": \"tag-val-3\"}");

        Pair<List<Object>,Integer> fetched1 = collection.fetch("{\"tag1\": \"tag-val-1\"}",0);
        Assert.assertEquals(fetched1.second.intValue(),2);

        Pair<List<Object>,Integer> fetched2 = collection.fetch("{\"tag2\": \"tag-val-2\"}",0);

        Assert.assertEquals(fetched2.second.intValue(),1);

        collection.selectDb("db2");
        collection.fetch("{}",0);
    }

    @Test
    public void testInWalletImmutableCollection(){
        //TODO test
        ConfTest confTest = ConfTest.newInstance();

        Agent agent1 = confTest.agent1();
        agent1.open();

      //  agent1: Agent

        InWalletImmutableCollection collection =   new  InWalletImmutableCollection(agent1.getWallet().getNonSecrets());


        JSONObject value1 = new JSONObject();
        value1.put("key1","value1");
        value1.put("key2",10000);

        JSONObject value2 = new JSONObject();
        value2.put("key1","value2");
        value2.put("key2",50000);

        collection.selectDb(UUID.randomUUID().toString());

        System.out.println("valu1="+value1.toString());
        System.out.println("valu2="+value2.toString());
        System.out.println("valu3="+"{\"tag\": \"value1\"}");
        System.out.println("valu4="+"{\"tag\": \"value2\"}");
        collection.add(value1.toString(),"{\"tag\": \"value1\"}");
        collection.add(value2.toString(),"{\"tag\": \"value2\"}");

        JSONObject query1 = new JSONObject();
        query1.put("tag","value1");


        Pair<List<Object>,Integer> result = collection.fetch(query1.toString());
        Assert.assertEquals(1, (int)result.second);
        Assert.assertEquals(1, result.first.size());
        Assert.assertEquals(value1.toString(), result.first.get(0));

        JSONObject query2 = new JSONObject();
        query2.put("tag","value2");
        Pair<List<Object>,Integer> result2 = collection.fetch(query2.toString());
        Assert.assertEquals(1, (int)result2.second);
        Assert.assertEquals(1, result2.first.size());
        Assert.assertEquals(value2.toString(), result2.first.get(0));


        JSONObject query3 = new JSONObject();

        Pair<List<Object>,Integer> result3 = collection.fetch(query3.toString());
        Assert.assertEquals(2, (int)result3.second);

        collection.selectDb(UUID.randomUUID().toString());


        JSONObject query4 = new JSONObject();
        Pair<List<Object>,Integer> result4 = collection.fetch(query4.toString());
        Assert.assertEquals(0, (int)result4.second);

        agent1.close();
    }
}
/*



@pytest.mark.asyncio
async def test_inwallet_immutable_collection(agent1: Agent):
        await agent1.open()
        try:
        collection = InWalletImmutableCollection(agent1.wallet.non_secrets)

        value1 = {
        'key1': 'value1',
        'key2': 10000
        }
        value2 = {
        'key1': 'value2',
        'key2': 50000
        }

        await collection.select_db(db_name=uuid.uuid4().hex)
        await collection.add(value1, {'tag': 'value1'})
        await collection.add(value2, {'tag': 'value2'})

        fetched, count = await collection.fetch({'tag': 'value1'})
        assert count == 1
        assert len(fetched) == 1
        assert fetched[0] == value1

        fetched, count = await collection.fetch({'tag': 'value2'})
        assert count == 1
        assert len(fetched) == 1
        assert fetched[0] == value2

        fetched, count = await collection.fetch({})
        assert count == 2

        await collection.select_db(db_name=uuid.uuid4().hex)
        fetched, count = await collection.fetch({})
        assert count == 0
        finally:
        await agent1.close()
*/
