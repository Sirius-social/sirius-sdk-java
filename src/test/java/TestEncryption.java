
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.encryption.Custom;
import com.sirius.sdk.encryption.Ed25519;
import com.sirius.sdk.encryption.UnpackModel;
import com.sirius.sdk.errors.sirius_exceptions.SiriusCryptoError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;
import com.sirius.sdk.messaging.Message;
import com.sirius.sdk.rpc.Future;
import com.sirius.sdk.rpc.Parsing;
import com.sirius.sdk.utils.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestEncryption {
    String seed1 = "000000000000000000000000000SEED1";
    String seed2 = "000000000000000000000000000SEED2";

    public String getTestMessage(){
        String string = "Test encryption строка";
        JSONObject enc_message = new JSONObject();
        enc_message.put("content", string);
        String message = enc_message.toString();
        return  StringUtils.escapeStringLikePython(message);
    }

    public String getTestMessage2(){
        Long  expirationTime = 1635520202L;
        String  msgType = "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/sirius_rpc/1.0/ping_agent";

        Future.FuturePromise future = new Future.FuturePromise("12","redis://redis/23423423423-909", expirationTime);
        Message request  = Parsing.buildRequest(msgType, future, null);
        request.setId("a9ab256b-dd19-47fe-973a-55501443101e");
        return request.serialize();
    }


    @Test
    public void encrypt() throws SiriusCryptoError, SodiumException, SiriusInvalidType {
        //CREATE KEYPAIR
        KeyPair keyPairRecipient = Custom.createKeypair(seed1.getBytes(StandardCharsets.US_ASCII));
        String verkeyRecipient = Custom.bytesToB58(keyPairRecipient.getPublicKey().getAsBytes());
        String sigkeyRecipient = Custom.bytesToB58(keyPairRecipient.getSecretKey().getAsBytes());

        KeyPair keyPairSender = Custom.createKeypair(seed2.getBytes(StandardCharsets.US_ASCII));
        String verkeySender = Custom.bytesToB58(keyPairSender.getPublicKey().getAsBytes());
        String sigkeySender = Custom.bytesToB58(keyPairSender.getSecretKey().getAsBytes());

        //CREATE TESTmessage
        String message = getTestMessage();
        Ed25519 ed25519 = new Ed25519();



        //PACK MESSAGE
        List<String> verkeys = new ArrayList<>();
        verkeys.add(verkeyRecipient);
        String packedString = ed25519.packMessage(message, verkeys, verkeySender, sigkeySender);
      //  String packedString  = "{\"protected\":\"eyJlbmMiOiJ4Y2hhY2hhMjBwb2x5MTMwNV9pZXRmIiwidHlwIjoiSldNLzEuMCIsImFsZyI6IkF1dGhjcnlwdCIsInJlY2lwaWVudHMiOlt7ImVuY3J5cHRlZF9rZXkiOiJ3M2RQVE5zbnUxQWk3dVo5U3VjQmpsLVJVYUtBd0oxUUVTSjZ4QlZkeUxBNkZudG5OdEdtS0NiZ05yNVhWc25pIiwiaGVhZGVyIjp7ImtpZCI6IjRQUXNYOGZ1Qmd1ZXJTYUJiVFF1R2h0UzhyZ0N6ODJYSm1lVEZNOVZHUEZqIiwic2VuZGVyIjoidjFfWlpNVFhDWm8xNXpPT1BOUUJxNTJRM3UyMHVfWDNWNDRJNS1aUUZDMEVBd2VscmJkdEZ0b0pXaF95Q05TRmJuZUxpYXVuZVc4VGZfUUdHUTdJMHdlZm1jRXdwcHJVX3BRZHdSeXNoVW9ZQS1SU0hwZG9wNUhIeGFRPSIsIml2IjoiclJrQTlvb2ZmRXc0bFc4OGt2SEhhNVF3ZUJhUzBoVnUifX1dfQ==\",\"iv\":\"W6sYp9kv38nVFdPdZvePz_62YrSyL36a\",\"ciphertext\":\"BGcejRmhN5BXykBhW0YlMB88I6TClOP4dSy9xSxO7ol-mmjQwXPQEqXGkfaoE1-fwbq94qhdB-r09thS7jp8IGDpRk7AK5ZOQFg4GlzUQ_6mMZceaF-OKuelBefnTwomf3nb222oqq_poGcPkWRyqXjcNrhEVV_KEH1EtSWbdlUO5hA8sj5_lyfz5jwiAf-kcHs1TG6JVYH7DIFq7Z_xYxlxgUOSWorU-DSkOfZLMmsIsroE4xpBxVR3jK_2r20lpS11PgLFHM_zO_fb-ffwha9MI9saKxgaAZNvnrr3HM3MzB9Vi1_e8XXxjjxs8JRJT9InLJqjoX2r3h4qLP7_IY_B84bIie_tlmeFHDqVwJYUHWQyQhjpNrOSFUJ65UYp49rE-Fa-jOOlB-H9baRW_T6I0DkCeQjInDEKmRKN2-9264IGVDxwKEUYPCbx5iITMYFpLZS2RwrY8G5PQurpSLJW2O2j1uCV4qZDnjk9aDmceIDf\",\"tag\":\"pg_WIq-2xeayvbjYT-BR6g==\"}";
        //UNPACK MESSAGE
        UnpackModel unpackedModel = ed25519.unpackMessage(packedString, verkeyRecipient, sigkeyRecipient);
        System.out.println("unpackedModel.getMessage()="+unpackedModel.getMessage());
        //ASSERTING
        Assert.assertEquals(unpackedModel.getSender_vk(),verkeySender);
        Assert.assertEquals(unpackedModel.getRecip_vk(),verkeyRecipient);
        Assert.assertEquals(message,unpackedModel.getMessage());
    }


    //read={"protected":"eyJlbmMiOiJ4Y2hhY2hhMjBwb2x5MTMwNV9pZXRmIiwidHlwIjoiSldNLzEuMCIsImFsZyI6IkF1dGhjcnlwdCIsInJlY2lwaWVudHMiOlt7ImVuY3J5cHRlZF9rZXkiOiJYT2x2emVXcm5EdGhWUEEyN2wwZVp3NFBBOUxJT0x0WUwwRjdEcTBwNUR3c1N6RjA1bVU0a0hITnJPdWtweE9LIiwiaGVhZGVyIjp7ImtpZCI6IjZRdlEzWTVwUE1HTmd6dnM4Nk4zQVFvOThwRjVXcnpNMWg2V2tLSDNkTDdmIiwiaXYiOiJVbEo4NlRxM0E2aVlDbi1NNnZfeUJHWkxZbDZjMzFvTiIsInNlbmRlciI6IjM1b1h4azI5dDVzVTVvSF9BRnhNUmx0RTZqMWNKUlRhaV9pRmx3RDNXaGM4YlhHMkdrbGxhMjQ1aE1waVdFVFJnYWdxNjYyeGtnRXFvcEFSd2UzanZ6bzV0VkhrWTlhN2pvM095RE1GOEtNcU9fMEhjMmk1bjJhZ05qTT0ifX1dfQ==","iv":"YINZWei8GWFxaw0o","ciphertext":"NMXqUXeGzxDxcFhTDW2QU0G3DPShSydosdMtFhRUVDw7Fl9mzqP3m9AiDT5IlfywOhyRTkkS5KfO9lcAv3PU46q6kFQNnoYf9eddALmoGmPFESJKF5gwItEdzFtUCuozzWcCi1kY85sJO9D-JOzFa-SZCluBftBbMU6qcgM_2vWm1iPP2CBLK5nQAfcDQzj5L_tkB5CMcXTMv1Wbq0EPIDT2_kFXi9Dn7L8eLWAWCmjMbdU80qXzX6KsntXSJ_ibKlEiGrvR3_clyg21L2xwzWlS9GBgv1P28dyC1Ofcd2xPpQclvg7e5nSB5scoKgsDrCdHX7_DllXsMA7uymwj7mIV9rifw4zwkldH_LApajYXbEpod-uEeN0KFu5TyhmwKKCfALtBZ6CctrqOLYm6D-rJCKzP7gUjfWKxwsNiXrhIy38LCQrO25nJ7Z8NPSbIaktpRiMJbz4oaJrmdvcjXVR1d-e8uDzRkvwvyEBRVFJKpuBKP9E-4HHLXh_F-6A=","tag":"8tOGZg-i1AO9RO0Eh2mh5Q=="}

    @Test
    public void test_fixture() throws SiriusCryptoError, SodiumException, SiriusInvalidType {
        KeyPair keyPairRecipient = null;
        //CREATE KEYPAIR
        keyPairRecipient = Custom.createKeypair(seed1.getBytes(StandardCharsets.US_ASCII));
        String verkey_recipient = Custom.bytesToB58(keyPairRecipient.getPublicKey().getAsBytes());
        String sigkey_recipient = Custom.bytesToB58(keyPairRecipient.getSecretKey().getAsBytes());

        KeyPair keyPairSender = Custom.createKeypair(seed2.getBytes(StandardCharsets.US_ASCII));
        String verkeySender = Custom.bytesToB58(keyPairSender.getPublicKey().getAsBytes());
        String sigkeySender = Custom.bytesToB58(keyPairSender.getSecretKey().getAsBytes());
        String packed= "{\"protected\": \"eyJlbmMiOiAieGNoYWNoYTIwcG9seTEzMDVfaWV0ZiIsICJ0eXAiOiAiSldNLzEuMCIsICJhbGciOiAiQXV0aGNyeXB0IiwgInJlY2lwaWVudHMiOiBbeyJlbmNyeXB0ZWRfa2V5IjogInBKcW1xQS1IVWR6WTNWcFFTb2dySGx4WTgyRnc3Tl84YTFCSmtHU2VMT014VUlwT0RQWTZsMVVsaVVvOXFwS0giLCAiaGVhZGVyIjogeyJraWQiOiAiM1ZxZ2ZUcDZRNFZlRjhLWTdlVHVXRFZBWmFmRDJrVmNpb0R2NzZLR0xtZ0QiLCAic2VuZGVyIjogIjRlYzhBeFRHcWtxamd5NHlVdDF2a0poeWlYZlNUUHo1bTRKQjk1cGZSMG1JVW9KajAwWmswNmUyUEVDdUxJYmRDck8xeTM5LUhGTG5NdW5YQVJZWk5rZ2pyYV8wYTBQODJpbVdNcWNHc1FqaFd0QUhOcUw1OGNkUUYwYz0iLCAiaXYiOiAiVU1PM2o1ZHZwQnFMb2Rvd3V0c244WEMzTkVqSWJLb2oifX1dfQ==\", \"iv\": \"MchkHF2M-4hneeUJ\", \"ciphertext\": \"UgcdsV-0rIkP25eJuRSROOuqiTEXp4NToKjPMmqqtJs-Ih1b5t3EEbrrHxeSfPsHtlO6J4OqA1jc5uuD3aNssUyLug==\", \"tag\": \"sQD8qgJoTrRoyQKPeCSBlQ==\"}";
        Ed25519 ed25519 = new Ed25519();
        //UNPACK MESSAGE
        UnpackModel unpackedModel = ed25519.unpackMessage(packed, verkey_recipient, sigkey_recipient);
        //В pytone при json.dumps добавляется пробел между ключом значением.
        String testMessage =  "{\"content\": \"Test encryption \\u0441\\u0442\\u0440\\u043e\\u043a\\u0430\"}";
        //ASSERTING
        Assert.assertEquals(unpackedModel.getSender_vk(),verkeySender);
        Assert.assertEquals( unpackedModel.getRecip_vk(),verkey_recipient);
        Assert.assertEquals( unpackedModel.getMessage(),testMessage);
    }

    @Test
    public void test_CryptoSign() throws SiriusCryptoError, SodiumException {
        KeyPair kp = Custom.createKeypair("0000000000000000000000000000SEED".getBytes(StandardCharsets.UTF_8));
        String msg = "message";
        byte[] signature = Custom.signMessage(msg.getBytes(StandardCharsets.UTF_8), kp.getSecretKey().getAsBytes());
        Assert.assertEquals("3tfqJYZ8ME8gTFUSHcH4uVTUx5kV7S1qPJJ65k2VtSocMfXvnzR1sbbfq6F2RcXrFtaufjEr4KQVu7aeyirYrcRm",
                Custom.bytesToB58(signature));

        Assert.assertTrue(Custom.verifySignedMessage(kp.getPublicKey().getAsBytes(),
                msg.getBytes(StandardCharsets.UTF_8), signature));

        KeyPair kp2 = Custom.createKeypair("000000000000000000000000000SEED2".getBytes(StandardCharsets.UTF_8));
        Assert.assertNotEquals(kp2.getPublicKey().getAsBytes(), kp.getPublicKey().getAsBytes());
        signature = Custom.signMessage(msg.getBytes(StandardCharsets.UTF_8), kp2.getSecretKey().getAsBytes());
        Assert.assertFalse(Custom.verifySignedMessage(kp.getPublicKey().getAsBytes(),
                msg.getBytes(StandardCharsets.UTF_8), signature));
    }

    @Test
    public void test_didFromVerkey() throws SiriusCryptoError, SodiumException {
        KeyPair kp = Custom.createKeypair("0000000000000000000000000000SEED".getBytes(StandardCharsets.UTF_8));
        Assert.assertEquals("GXhjv2jGf2oT1sqMyvJtgJxNYPMHmTsdZ3c2ZYQLJExj",
                Custom.bytesToB58(kp.getPublicKey().getAsBytes()));
        byte[] did = Custom.didFromVerkey(kp.getPublicKey().getAsBytes());
        Assert.assertEquals("VVZbGvuFqBdoVNY1Jh4j9Q", Custom.bytesToB58(did));
    }

}
