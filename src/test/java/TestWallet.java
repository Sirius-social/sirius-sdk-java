import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.wallet.DynamicWallet;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.AnonCredSchema;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.CacheOptions;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.NYMRole;
import com.sirius.sdk.agent.wallet.abstract_wallet.model.RetrieveRecordOptions;
import com.sirius.sdk.utils.Pair;
import helpers.ConfTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestWallet {

    ConfTest confTest;

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testCryptoPackMessage() {
        Agent agent1 = confTest.agent1();
        Agent agent2 = confTest.agent2();
        agent1.open();
        agent2.open();

        DynamicWallet walletSender = agent1.getWallet();
        DynamicWallet walletRecipient = agent2.getWallet();

        String verkeySender = walletSender.getCrypto().createKey();
        String verkeyRecipient = walletRecipient.getCrypto().createKey();
        Assert.assertNotNull(verkeySender);
        Assert.assertNotNull(verkeyRecipient);
        List<String> verKeyList = new ArrayList<>();
        verKeyList.add(verkeyRecipient);
        JSONObject message = new JSONObject();
        message.put("content", "Hello!");
        //1: anon crypt mode
        byte[] messageWired = walletSender.getCrypto().packMessage(message, verKeyList);
        String unpackedMessage = walletRecipient.getCrypto().unpackMessage(messageWired);
        JSONObject jsonObject = new JSONObject(unpackedMessage);
        JSONObject messObjUnpacked = jsonObject.getJSONObject("message");
        Assert.assertEquals(message.toString(), messObjUnpacked.toString());
        //2: auth crypt mode
        byte[] messageWired2 = walletSender.getCrypto().packMessage(message, verKeyList, verkeySender);
        String unpackedMessage2 = walletRecipient.getCrypto().unpackMessage(messageWired2);

        JSONObject jsonObject2 = new JSONObject(unpackedMessage2);
        JSONObject messObjUnpacked2 = jsonObject2.getJSONObject("message");

        Assert.assertEquals(message.toString(), messObjUnpacked2.toString());
        Assert.assertNotEquals(messageWired2, messageWired);

        agent1.close();
        agent2.close();
    }

    @Test
    public void testCryptoSign() {
        Agent agent1 = confTest.agent1();
        Agent agent2 = confTest.agent2();
        agent1.open();
        agent2.open();

        DynamicWallet walletSigner = agent1.getWallet();
        DynamicWallet walletVerifier = agent2.getWallet();

        String keySigner = walletSigner.getCrypto().createKey();
        JSONObject message = new JSONObject();
        message.put("content", "Hello!");

        byte[] messageBytes = message.toString().getBytes(StandardCharsets.US_ASCII);

        byte[] signature = walletSigner.getCrypto().cryptoSign(keySigner, messageBytes);
        boolean isOk = walletVerifier.getCrypto().cryptoVerify(keySigner, messageBytes, signature);

        Assert.assertTrue(isOk);


        String keySigner2 = walletSigner.getCrypto().createKey();
        byte[] brokenSignature = walletSigner.getCrypto().cryptoSign(keySigner, messageBytes);
        boolean isOk2 = walletVerifier.getCrypto().cryptoVerify(keySigner2, messageBytes, brokenSignature);
        Assert.assertFalse(isOk2);


        agent1.close();
        agent2.close();
    }


    @Test
    public void testDidMaintenance() {
        Agent agent1 = confTest.agent1();
        agent1.open();

        //1: Create Key
        String randomKey = agent1.getWallet().getDid().createKey();
        Assert.assertNotNull(randomKey);

        // 2: Set metadata
        JSONObject metadataObject = new JSONObject();
        metadataObject.put("key1", "value1");
        metadataObject.put("key2", "value2");

        agent1.getWallet().getDid().setKeyMetadata(randomKey, metadataObject.toString());
        String actualMetadata = agent1.getWallet().getDid().getKeyMetadata(randomKey);

        Assert.assertEquals(metadataObject.toString(), actualMetadata);

        // 3:  Create DID + Verkey

        Pair<String, String> didVerkey = agent1.getWallet().getDid().createAndStoreMyDid();
        String fully = agent1.getWallet().getDid().qualifyDid(didVerkey.first, "peer");

        Assert.assertTrue(fully.contains(didVerkey.first));


        // 4:  Replace verkey
        String verkeyNew = agent1.getWallet().getDid().replaceKeysStart(fully);

        Assert.assertNotNull(verkeyNew);

        List<Object> metadataList = agent1.getWallet().getDid().listMyDidsWithMeta();
        System.out.println("metadataList=" + metadataList);
        Assert.assertNotNull(metadataList);
        boolean anyTempVerkey = false;
        for (int i = 0; i < metadataList.size(); i++) {
            Object m = metadataList.get(i);
            if (m instanceof JSONObject) {
                String tempVerKey = ((JSONObject) m).optString("tempVerkey");
                if (verkeyNew.equals(tempVerKey)) {
                    anyTempVerkey = true;
                }
            }
            System.out.println("m=" + m);
        }
        Assert.assertTrue(anyTempVerkey);


        agent1.getWallet().getDid().replaceKeysApply(fully);
        List<Object> metadataList2 = agent1.getWallet().getDid().listMyDidsWithMeta();

        //  assert any([m['verkey'] == verkey_new for m in metadata_list])

        Assert.assertNotNull(metadataList2);
        boolean anyTempVerkey2 = false;
        for (int i = 0; i < metadataList2.size(); i++) {
            Object m = metadataList2.get(i);
            if (m instanceof JSONObject) {
                String verKey = ((JSONObject) m).optString("verkey");
                if (verkeyNew.equals(verKey)) {
                    anyTempVerkey2 = true;
                }
            }

        }
        Assert.assertTrue(anyTempVerkey2);


        String actualVerkey = agent1.getWallet().getDid().keyForLocalDid(fully);
        Assert.assertEquals(verkeyNew, actualVerkey);

        agent1.close();
    }

    @Test
    public void testTheirDidMaintenance() {
        Agent agent1 = confTest.agent1();
        Agent agent2 = confTest.agent2();
        agent1.open();
        agent2.open();

        DynamicWallet walletMe = agent1.getWallet();
        DynamicWallet walletTheir = agent2.getWallet();

        Pair<String, String> myDidVerkey = walletMe.getDid().createAndStoreMyDid();
        Pair<String, String> theirDidVerkey = walletTheir.getDid().createAndStoreMyDid();

        walletMe.getDid().storeTheirDid(theirDidVerkey.first, theirDidVerkey.second);

        JSONObject metadataObject = new JSONObject();
        metadataObject.put("key1", "value1");
        metadataObject.put("key2", "value2");

        walletMe.getDid().setDidMetadata(theirDidVerkey.first, metadataObject.toString());

        String metadataExpected = metadataObject.toString();
        String metadataActual = walletMe.getDid().getDidMetadata(theirDidVerkey.first);

        Assert.assertEquals(metadataExpected, metadataActual);

        String verkey = walletMe.getDid().keyForLocalDid(theirDidVerkey.first);

        Assert.assertEquals(theirDidVerkey.second, verkey);

        String verkeyTheirNew = walletTheir.getDid().replaceKeysStart(theirDidVerkey.first);

        walletTheir.getDid().replaceKeysApply(theirDidVerkey.first);
        walletMe.getDid().storeTheirDid(theirDidVerkey.first, verkeyTheirNew);

        String verkeyNew = walletMe.getDid().keyForLocalDid(theirDidVerkey.first);

        Assert.assertEquals(verkeyTheirNew, verkeyNew);

        agent1.close();
        agent2.close();
    }


    @Test
    public void testRecordValue() {
        Agent agent1 = confTest.agent1();
        agent1.open();

        String value = "my-value-" + UUID.randomUUID().toString();
        String myId = "my-id-" + UUID.randomUUID().toString();
        agent1.getWallet().getNonSecrets().addWalletRecord("type", myId, value);
        RetrieveRecordOptions opts = new RetrieveRecordOptions();
        opts.checkAll();

        String valueInfo = agent1.getWallet().getNonSecrets().getWalletRecord("type", myId, opts);
        Assert.assertNotNull(valueInfo);
        JSONObject valueInfoObject = new JSONObject(valueInfo);

        Assert.assertEquals(myId, valueInfoObject.getString("id"));
        Assert.assertEquals(new JSONObject().toString(), valueInfoObject.optJSONObject("tags").toString());
        Assert.assertEquals(value, valueInfoObject.getString("value"));
        Assert.assertEquals("type", valueInfoObject.getString("type"));

        String valueNew = "my-new-value-" + UUID.randomUUID().toString();

        agent1.getWallet().getNonSecrets().updateWalletRecordValue("type", myId, valueNew);

        String valueInfoNew = agent1.getWallet().getNonSecrets().getWalletRecord("type", myId, opts);
        JSONObject valueInfoObjectNew = new JSONObject(valueInfoNew);

        Assert.assertEquals(valueNew, valueInfoObjectNew.getString("value"));

        agent1.getWallet().getNonSecrets().deleteWalletRecord("type", myId);

        agent1.close();
    }


    @Test
    public void testRecordValueWithTags() {
        Agent agent1 = confTest.agent1();
        agent1.open();

        String value = "my-value-" + UUID.randomUUID().toString();
        String myId = "my-id-" + UUID.randomUUID().toString();
        JSONObject tags = new JSONObject();
        tags.put("tag1", "val1");
        tags.put("~tag2", "val2");

        agent1.getWallet().getNonSecrets().addWalletRecord("type", myId, value, tags.toString());
        RetrieveRecordOptions opts = new RetrieveRecordOptions();
        opts.checkAll();

        String valueInfo = agent1.getWallet().getNonSecrets().getWalletRecord("type", myId, opts);
        JSONObject valueInfoObject = new JSONObject(valueInfo);

        Assert.assertEquals(myId, valueInfoObject.getString("id"));
        Assert.assertEquals(tags.toString(), valueInfoObject.optJSONObject("tags").toString());
        Assert.assertEquals(value, valueInfoObject.getString("value"));
        Assert.assertEquals("type", valueInfoObject.getString("type"));

        JSONObject updTags = new JSONObject();
        updTags.put("ext-tag", "val3");
        agent1.getWallet().getNonSecrets().updateWalletRecordTags("type", myId, updTags.toString());

        String valueInfoNew = agent1.getWallet().getNonSecrets().getWalletRecord("type", myId, opts);
        JSONObject valueInfoNewObject = new JSONObject(valueInfoNew);

        Assert.assertEquals(updTags.toString(), valueInfoNewObject.optJSONObject("tags").toString());

        agent1.getWallet().getNonSecrets().addWalletRecordTags("type", myId, tags.toString());


        String valueInfoNew2 = agent1.getWallet().getNonSecrets().getWalletRecord("type", myId, opts);
        JSONObject valueInfoNew2Object = new JSONObject(valueInfoNew2);

        updTags.put("tag1", "val1");
        updTags.put("~tag2", "val2");

        Assert.assertEquals(updTags.toString(), valueInfoNew2Object.optJSONObject("tags").toString());
        List<String> tagsList = new ArrayList<>();
        tagsList.add("ext-tag");
        agent1.getWallet().getNonSecrets().deleteWalletRecord("type", myId, tagsList);

        String valueInfoNew3 = agent1.getWallet().getNonSecrets().getWalletRecord("type", myId, opts);
        JSONObject valueInfoNew3Object = new JSONObject(valueInfoNew3);

        Assert.assertEquals(tags.toString(), valueInfoNew3Object.optJSONObject("tags").toString());

        agent1.close();
    }

    @Test
    public void testRecordValueWithTagsThenUpdate() {
        Agent agent1 = confTest.agent1();
        agent1.open();

        String value = "my-value-" + UUID.randomUUID().toString();
        String myId = "my-id-" + UUID.randomUUID().toString();

        agent1.getWallet().getNonSecrets().addWalletRecord("type", myId, value);
        RetrieveRecordOptions opts = new RetrieveRecordOptions();
        opts.checkAll();

        String valueInfo = agent1.getWallet().getNonSecrets().getWalletRecord("type", myId, opts);
        JSONObject valueInfoObject = new JSONObject(valueInfo);

        Assert.assertEquals(myId, valueInfoObject.getString("id"));
        Assert.assertEquals(new JSONObject().toString(), valueInfoObject.optJSONObject("tags").toString());
        Assert.assertEquals(value, valueInfoObject.getString("value"));
        Assert.assertEquals("type", valueInfoObject.getString("type"));

        JSONObject tags1 = new JSONObject();
        tags1.put("tag1", "val1");
        tags1.put("~tag2", "val2");

        agent1.getWallet().getNonSecrets().updateWalletRecordTags("type", myId, tags1.toString());

        String valueInfo1 = agent1.getWallet().getNonSecrets().getWalletRecord("type", myId, opts);
        JSONObject valueInfo1Object = new JSONObject(valueInfo1);

        Assert.assertEquals(tags1.toString(), valueInfo1Object.optJSONObject("tags").toString());

        JSONObject tags2 = new JSONObject();
        tags1.put("tag3", "val3");

        agent1.getWallet().getNonSecrets().updateWalletRecordTags("type", myId, tags2.toString());

        String valueInfo2 = agent1.getWallet().getNonSecrets().getWalletRecord("type", myId, opts);
        JSONObject valueInfo2Object = new JSONObject(valueInfo2);

        Assert.assertEquals(tags2.toString(), valueInfo2Object.optJSONObject("tags").toString());

        agent1.close();

    }


    @Test
    public void testRecordSearch() {
        Agent agent1 = confTest.agent1();
        agent1.open();

        String id1 = "id-1-" + UUID.randomUUID().toString();
        String id2 = "id-2-" + UUID.randomUUID().toString();

        String value1 = "value-1-" + UUID.randomUUID().toString();
        String value2 = "value-2-" + UUID.randomUUID().toString();

        String markerA = "A-" + UUID.randomUUID().toString();
        String markerB = "B-" + UUID.randomUUID().toString();

        RetrieveRecordOptions opts = new RetrieveRecordOptions();
        opts.checkAll();

        JSONObject tags1 = new JSONObject();
        tags1.put("tag1", value1);
        tags1.put("~tag2", "5");
        tags1.put("marker", markerA);

        JSONObject tags2 = new JSONObject();
        tags2.put("tag3", "val3");
        tags2.put("~tag4", value2);
        tags2.put("marker", markerB);

        agent1.getWallet().getNonSecrets().addWalletRecord("type", id1, "value1", tags1.toString());
        agent1.getWallet().getNonSecrets().addWalletRecord("type", id2, "value2", tags2.toString());

        JSONObject query = new JSONObject();
        query.put("tag1", value1);

        Pair<List<String>, Integer> recordsTotal = agent1.getWallet().getNonSecrets().walletSearch("type", query.toString(), opts);

        List<String> searchList = recordsTotal.first;
        System.out.println("searchList=" + searchList);
        System.out.println("recordsTotal.second=" + recordsTotal.second);

        Assert.assertEquals(1, (int) recordsTotal.second);
        for (int i = 0; i < recordsTotal.first.size(); i++) {
            Assert.assertTrue((recordsTotal.first.get(i).toString()).contains(value1));
        }


        JSONObject queryNew = new JSONObject();
        JSONArray queryArr = new JSONArray();
        JSONObject querytag1 = new JSONObject();
        querytag1.put("tag1", value1);
        JSONObject querytag2 = new JSONObject();
        querytag2.put("~tag4", value2);
        queryArr.put(querytag1);
        queryArr.put(querytag2);
        queryNew.put("$or", queryArr);

        Pair<List<String>, Integer> recordsTotal2 = agent1.getWallet().getNonSecrets().walletSearch("type", queryNew.toString(), opts);
        List<String> searchList2 = recordsTotal2.first;

        Assert.assertEquals(recordsTotal2.first.size(), 1);
        Assert.assertEquals((int) recordsTotal2.second, 2);


        Pair<List<String>, Integer> recordsTotal3 = agent1.getWallet().getNonSecrets().walletSearch("type", queryNew.toString(), opts, 1000);

        Assert.assertEquals(recordsTotal3.first.size(), 2);
        Assert.assertEquals((int) recordsTotal3.second, 2);

        JSONObject queryNew1 = new JSONObject();
        JSONObject queryArg = new JSONObject();
        JSONArray queryArr1 = new JSONArray();
        queryArr1.put(markerA);
        queryArr1.put(markerB);
        queryArg.put("$in", queryArr1);

        queryNew1.put("marker", queryArg);

        Pair<List<String>, Integer> recordsTotal4 = agent1.getWallet().getNonSecrets().walletSearch("type", queryNew1.toString(), opts, 1000);

        Assert.assertEquals((int) recordsTotal4.second, 2);

        agent1.close();
    }


    @Test
    public void testRegisterSchemaInNetwork() {
        Agent agent2 = confTest.agent2();
        agent2.open();
        String seed = "000000000000000000000000Trustee1";

        Pair<String, String> didVerkey = agent2.getWallet().getDid().createAndStoreMyDid(null, seed);
        String schema_name = "schema_" + UUID.randomUUID().toString();

        List<String> attibutes = new ArrayList<>();
        attibutes.add("attr1");
        attibutes.add("attr2");
        attibutes.add("attr3");
        Pair<String, AnonCredSchema> schemaIdSchema = agent2.getWallet().getAnoncreds().issuerCreateSchema(didVerkey.first, schema_name, "1.0", attibutes);

        System.out.println("schemaIdSchema=" + schemaIdSchema);


        Pair<Boolean, String> response = agent2.getWallet().getLedger().registerSchema(confTest.defaultNetwork(), didVerkey.first, schemaIdSchema.second);

        System.out.println("response=" + response);

        Assert.assertTrue(response.first);

        agent2.close();
    }

    @Test
    public void testRegisterCredDefInNetwork() {
        Agent agent2 = confTest.agent2();
        String defaultNetwork = confTest.defaultNetwork();

        agent2.open();
        String seed = "000000000000000000000000Trustee1";
        Pair<String, String> didVerkey = agent2.getWallet().getDid().createAndStoreMyDid(null, seed);
        String schemaName = "schema_" + UUID.randomUUID().toString();
        Pair<String, AnonCredSchema> anoncreds = agent2.getWallet().getAnoncreds().issuerCreateSchema(didVerkey.first, schemaName, "1.0", "attr1", "attr2", "attr3");

        Pair<Boolean, String> okResponse = agent2.getWallet().getLedger().registerSchema(defaultNetwork, didVerkey.first, anoncreds.second);

        Assert.assertTrue(okResponse.first);

        CacheOptions opt = new CacheOptions();

        String schemaForLedger = agent2.getWallet().getCache().getSchema(defaultNetwork, didVerkey.first, anoncreds.first, opt);
        Pair<String, String> credDefIdCreddef = agent2.getWallet().getAnoncreds().issuerCreateAndStoreCredentialDef(didVerkey.first, new JSONObject(schemaForLedger), "TAG");

        Pair<Boolean, String> okResponse2 = agent2.getWallet().getLedger().registerCredDef(defaultNetwork, didVerkey.first, new JSONObject(credDefIdCreddef.second));
        Assert.assertTrue(okResponse2.first);

        agent2.close();
    }


    @Test
    public void testNymOperationsInNetwork() {
        Agent agent1 = confTest.agent1();
        Agent agent2 = confTest.agent2();

        agent1.open();
        agent2.open();
        try {
            DynamicWallet steward = agent1.getWallet();
            DynamicWallet actor = agent2.getWallet();

            String seed = "000000000000000000000000Steward1";

            Pair<String, String> didVerkeySteward = steward.getDid().createAndStoreMyDid(null, seed);

            Pair<String, String> didTrusteeVerkeTrustee = actor.getDid().createAndStoreMyDid();
            Pair<String, String> didCommonVerkeyCommon = actor.getDid().createAndStoreMyDid();

            //   # Trust Anchor
            Pair<Boolean, String> okResponse = steward.getLedger().writeNum(confTest.defaultNetwork(), didVerkeySteward.first,
                    didTrusteeVerkeTrustee.first, didTrusteeVerkeTrustee.second, "Test-Trustee", NYMRole.TRUST_ANCHOR);
            Assert.assertTrue(okResponse.first);

            Pair<Boolean, String> okNym1 = steward.getLedger().readNym(confTest.defaultNetwork(), didVerkeySteward.first, didTrusteeVerkeTrustee.first);
            Assert.assertTrue(okNym1.first);

            Pair<Boolean, String> okNym2 = steward.getLedger().readNym(confTest.defaultNetwork(), null, didTrusteeVerkeTrustee.first);
            Assert.assertTrue(okNym2.first);

            Assert.assertEquals(okNym1.second, okNym2.second);
            JSONObject okNymJson = new JSONObject(okNym1.second);

            Integer role = okNymJson.getInt("role");
            Assert.assertEquals(role, NYMRole.TRUST_ANCHOR.getValue());

            //Common User
            Pair<Boolean, String> okResponseCommon = steward.getLedger().writeNum(confTest.defaultNetwork(), didVerkeySteward.first, didCommonVerkeyCommon.first,
                    didCommonVerkeyCommon.second, "CommonUser", NYMRole.COMMON_USER);

            Assert.assertTrue(okResponseCommon.first);

            Pair<Boolean, String> okNym3 = steward.getLedger().readNym(confTest.defaultNetwork(), null, didCommonVerkeyCommon.first);
            Assert.assertTrue(okNym3.first);

            JSONObject okNym3Json = new JSONObject(okNym3.second);

            Object role3 = okNym3Json.get("role");
            Assert.assertEquals(role3, JSONObject.NULL);
            Pair<Boolean, String> okResponse3 = actor.getLedger().writeNum(confTest.defaultNetwork(), didCommonVerkeyCommon.first,
                    didCommonVerkeyCommon.first, didCommonVerkeyCommon.second, "ResetUser", NYMRole.RESET);

            Assert.assertTrue(okResponse3.first);

            Pair<Boolean, String> okNym4 = steward.getLedger().readNym(confTest.defaultNetwork(), null, didCommonVerkeyCommon.first);

            Assert.assertTrue(okNym4.first);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            agent1.close();
            agent2.close();
        }
    }


    @Test
    public void testAttributeOperationsInNetwork() {
        Agent agent1 = confTest.agent1();
        Agent agent2 = confTest.agent2();

        agent1.open();
        agent2.open();

        DynamicWallet steward = agent1.getWallet();
        DynamicWallet actor = agent2.getWallet();

        String seed = "000000000000000000000000Steward1";
        Pair<String, String> didVerkeySteward = steward.getDid().createAndStoreMyDid(null, seed);
        Pair<String, String> didCommonVerkeyCommo = actor.getDid().createAndStoreMyDid();

        Pair<Boolean, String> okResponse = steward.getLedger().writeNum(confTest.defaultNetwork(), didVerkeySteward.first,
                didCommonVerkeyCommo.first, didCommonVerkeyCommo.second, "CommonUser", NYMRole.COMMON_USER);
        Assert.assertTrue(okResponse.first);

        Pair<Boolean, String> okResponse2 = actor.getLedger().writeAttribute(confTest.defaultNetwork(), didCommonVerkeyCommo.first,
                didCommonVerkeyCommo.first, "attribute", "value");
        Assert.assertTrue(okResponse2.first);
        System.out.println(okResponse2);

        Pair<Boolean, String> okResponse4 = steward.getLedger().readAttribute(confTest.defaultNetwork(), didVerkeySteward.first,
                didCommonVerkeyCommo.first, "attribute");
        System.out.println(okResponse4);

        Assert.assertTrue(okResponse4.first);
        Assert.assertEquals(okResponse4.second, "value");
        agent1.close();
        agent2.close();
    }

}

