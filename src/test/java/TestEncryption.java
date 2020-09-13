
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.encryption.Custom;
import com.sirius.sdk.encryption.Ed25519;
import com.sirius.sdk.encryption.UnpackModel;
import com.sirius.sdk.errors.sirius_exceptions.SiriusCryptoError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;
import com.sirius.sdk.utils.StringUtils;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    @Test
    public void encrypt() {
        Custom custom = new Custom();
        try {
            //CREATE KEYPAIR
            KeyPair keyPairRecipient = custom.createKeypair(seed1.getBytes(StandardCharsets.US_ASCII));
            String verkeyRecipient = custom.bytesToB58(keyPairRecipient.getPublicKey().getAsBytes());
            String sigkeyRecipient = custom.bytesToB58(keyPairRecipient.getSecretKey().getAsBytes());

            KeyPair keyPairSender = custom.createKeypair(seed2.getBytes(StandardCharsets.US_ASCII));
            String verkeySender = custom.bytesToB58(keyPairSender.getPublicKey().getAsBytes());
            String sigkeySender = custom.bytesToB58(keyPairSender.getSecretKey().getAsBytes());

            //CREATE TESTmessage
            String message = getTestMessage();
            Ed25519 ed25519 = new Ed25519();



            //PACK MESSAGE
            List<String> verkeys = new ArrayList<>();
            verkeys.add(verkeyRecipient);
            String packedString = ed25519.packMessage(message, verkeys, verkeySender, sigkeySender);

            //UNPACK MESSAGE
            UnpackModel unpackedModel = ed25519.unpackMessage(packedString, verkeyRecipient, sigkeyRecipient);

            //ASSERTING
            Assert.assertEquals(unpackedModel.getSender_vk(),verkeySender);
            Assert.assertEquals(unpackedModel.getRecip_vk(),verkeyRecipient);
            Assert.assertEquals(message,unpackedModel.getMessage());
        } catch (SiriusCryptoError siriusCryptoError) {
            siriusCryptoError.printStackTrace();
        } catch (SodiumException e) {
            e.printStackTrace();
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }
    }


    //read={"protected":"eyJlbmMiOiJ4Y2hhY2hhMjBwb2x5MTMwNV9pZXRmIiwidHlwIjoiSldNLzEuMCIsImFsZyI6IkF1dGhjcnlwdCIsInJlY2lwaWVudHMiOlt7ImVuY3J5cHRlZF9rZXkiOiJYT2x2emVXcm5EdGhWUEEyN2wwZVp3NFBBOUxJT0x0WUwwRjdEcTBwNUR3c1N6RjA1bVU0a0hITnJPdWtweE9LIiwiaGVhZGVyIjp7ImtpZCI6IjZRdlEzWTVwUE1HTmd6dnM4Nk4zQVFvOThwRjVXcnpNMWg2V2tLSDNkTDdmIiwiaXYiOiJVbEo4NlRxM0E2aVlDbi1NNnZfeUJHWkxZbDZjMzFvTiIsInNlbmRlciI6IjM1b1h4azI5dDVzVTVvSF9BRnhNUmx0RTZqMWNKUlRhaV9pRmx3RDNXaGM4YlhHMkdrbGxhMjQ1aE1waVdFVFJnYWdxNjYyeGtnRXFvcEFSd2UzanZ6bzV0VkhrWTlhN2pvM095RE1GOEtNcU9fMEhjMmk1bjJhZ05qTT0ifX1dfQ==","iv":"YINZWei8GWFxaw0o","ciphertext":"NMXqUXeGzxDxcFhTDW2QU0G3DPShSydosdMtFhRUVDw7Fl9mzqP3m9AiDT5IlfywOhyRTkkS5KfO9lcAv3PU46q6kFQNnoYf9eddALmoGmPFESJKF5gwItEdzFtUCuozzWcCi1kY85sJO9D-JOzFa-SZCluBftBbMU6qcgM_2vWm1iPP2CBLK5nQAfcDQzj5L_tkB5CMcXTMv1Wbq0EPIDT2_kFXi9Dn7L8eLWAWCmjMbdU80qXzX6KsntXSJ_ibKlEiGrvR3_clyg21L2xwzWlS9GBgv1P28dyC1Ofcd2xPpQclvg7e5nSB5scoKgsDrCdHX7_DllXsMA7uymwj7mIV9rifw4zwkldH_LApajYXbEpod-uEeN0KFu5TyhmwKKCfALtBZ6CctrqOLYm6D-rJCKzP7gUjfWKxwsNiXrhIy38LCQrO25nJ7Z8NPSbIaktpRiMJbz4oaJrmdvcjXVR1d-e8uDzRkvwvyEBRVFJKpuBKP9E-4HHLXh_F-6A=","tag":"8tOGZg-i1AO9RO0Eh2mh5Q=="}

    @Test
    public void test_fixture() {
        Custom custom = new Custom();
        KeyPair keyPairRecipient = null;
        try {
            //CREATE KEYPAIR
            keyPairRecipient = custom.createKeypair(seed1.getBytes(StandardCharsets.US_ASCII));
            String verkey_recipient = custom.bytesToB58(keyPairRecipient.getPublicKey().getAsBytes());
            String sigkey_recipient = custom.bytesToB58(keyPairRecipient.getSecretKey().getAsBytes());

            KeyPair keyPairSender = custom.createKeypair(seed2.getBytes(StandardCharsets.US_ASCII));
            String verkeySender = custom.bytesToB58(keyPairSender.getPublicKey().getAsBytes());
            String sigkeySender = custom.bytesToB58(keyPairSender.getSecretKey().getAsBytes());
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
        } catch (SiriusCryptoError siriusCryptoError) {
            siriusCryptoError.printStackTrace();
        } catch (SodiumException e) {
            e.printStackTrace();
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }
    }


}
