import com.sirius.sdk.agent.Agent;
import com.sirius.sdk.agent.wallet.DynamicWallet;
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

        String verkeySender = walletSender.getCrypto().createKey(null, null);
        String verkeyRecipient = walletRecipient.getCrypto().createKey(null, null);
        Assert.assertNotNull(verkeySender);
        Assert.assertNotNull(verkeyRecipient);
        List<String> verKeyList = new ArrayList<>();
        verKeyList.add(verkeyRecipient);
        JSONObject message = new JSONObject();
        message.put("content", "Hello!");
        //1: anon crypt mode
        byte[] messageWired = walletSender.getCrypto().packMessage(message, verKeyList, null);
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

        String keySigner = walletSigner.getCrypto().createKey(null, null);
        JSONObject message = new JSONObject();
        message.put("content", "Hello!");

        byte[] messageBytes = message.toString().getBytes(StandardCharsets.US_ASCII);

        byte[] signature = walletSigner.getCrypto().cryptoSign(keySigner, messageBytes);
        boolean isOk = walletVerifier.getCrypto().cryptoVerify(keySigner, messageBytes, signature);

        Assert.assertTrue(isOk);


        String keySigner2 = walletSigner.getCrypto().createKey(null, null);
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
        String randomKey = agent1.getWallet().getDid().createKey(null);
        Assert.assertNotNull(randomKey);

        // 2: Set metadata
        JSONObject metadataObject = new JSONObject();
        metadataObject.put("key1", "value1");
        metadataObject.put("key2", "value2");

        agent1.getWallet().getDid().setKeyMetadata(randomKey, metadataObject.toString());
        String actualMetadata = agent1.getWallet().getDid().getKeyMetadata(randomKey);

        Assert.assertEquals(metadataObject.toString(), actualMetadata);

        // 3:  Create DID + Verkey

        Pair<String, String> didVerkey = agent1.getWallet().getDid().createAndStoreMyDid(null, null, null);
        String fully = agent1.getWallet().getDid().qualifyDid(didVerkey.first, "peer");

        Assert.assertTrue(fully.contains(didVerkey.first));


        // 4:  Replace verkey
        String verkeyNew = agent1.getWallet().getDid().replaceKeysStart(fully, null);

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

        Pair<String, String> myDidVerkey = walletMe.getDid().createAndStoreMyDid(null, null, null);
        Pair<String, String> theirDidVerkey = walletTheir.getDid().createAndStoreMyDid(null, null, null);

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

        String verkeyTheirNew = walletTheir.getDid().replaceKeysStart(theirDidVerkey.first, null);

        walletTheir.getDid().replaceKeysApply(theirDidVerkey.first);
        walletMe.getDid().storeTheirDid(theirDidVerkey.first, verkeyTheirNew);

        String verkeyNew = walletMe.getDid().keyForLocalDid(theirDidVerkey.first);

        Assert.assertEquals(verkeyTheirNew, verkeyNew);

        agent1.close();
        agent2.close();
    }

}
