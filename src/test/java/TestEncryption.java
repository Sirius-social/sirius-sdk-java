import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.encryption.Custom;
import com.sirius.sdk.encryption.Ed25519;
import com.sirius.sdk.encryption.UnpackModel;
import com.sirius.sdk.errors.sirius_exceptions.SiriusCryptoError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;
import org.json.JSONObject;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestEncryption {
    String seed1 = "000000000000000000000000000SEED1";
    String seed2 = "000000000000000000000000000SEED2";

    //    String encryptedTExt ="b'{"protected": "eyJlbmMiOiAieGNoYWNoYTIwcG9seTEzMDVfaWV0ZiIsICJ0eXAiOiAiSldNLzEuMCIsICJhbGciOiAiQXV0aGNyeXB0IiwgInJlY2lwaWVudHMiOiBbeyJlbmNyeXB0ZWRfa2V5IjogInBKcW1xQS1IVWR6WTNWcFFTb2dySGx4WTgyRnc3Tl84YTFCSmtHU2VMT014VUlwT0RQWTZsMVVsaVVvOXFwS0giLCAiaGVhZGVyIjogeyJraWQiOiAiM1ZxZ2ZUcDZRNFZlRjhLWTdlVHVXRFZBWmFmRDJrVmNpb0R2NzZLR0xtZ0QiLCAic2VuZGVyIjogIjRlYzhBeFRHcWtxamd5NHlVdDF2a0poeWlYZlNUUHo1bTRKQjk1cGZSMG1JVW9KajAwWmswNmUyUEVDdUxJYmRDck8xeTM5LUhGTG5NdW5YQVJZWk5rZ2pyYV8wYTBQODJpbVdNcWNHc1FqaFd0QUhOcUw1OGNkUUYwYz0iLCAiaXYiOiAiVU1PM2o1ZHZwQnFMb2Rvd3V0c244WEMzTkVqSWJLb2oifX1dfQ==", "iv": "MchkHF2M-4hneeUJ", "ciphertext": "UgcdsV-0rIkP25eJuRSROOuqiTEXp4NToKjPMmqqtJs-Ih1b5t3EEbrrHxeSfPsHtlO6J4OqA1jc5uuD3aNssUyLug==", "tag": "sQD8qgJoTrRoyQKPeCSBlQ=="}";
    @Test
    public void encrypt() {
        Custom custom = new Custom();
        try {
            KeyPair keyPairRecipient = custom.createKeypair(seed1.getBytes(StandardCharsets.US_ASCII));
            String verkeyRecipient = custom.bytesToB58(keyPairRecipient.getPublicKey().getAsBytes());
            String sigkeyRecipient = custom.bytesToB58(keyPairRecipient.getSecretKey().getAsBytes());

            KeyPair keyPairSender = custom.createKeypair(seed2.getBytes(StandardCharsets.US_ASCII));
            String verkeySender = custom.bytesToB58(keyPairSender.getPublicKey().getAsBytes());
            String sigkeySender = custom.bytesToB58(keyPairSender.getSecretKey().getAsBytes());
            String string = "Test encryption строка";
            JSONObject enc_message = new JSONObject();
            enc_message.put("content", string);
            String message = enc_message.toString();
            Ed25519 ed25519 = new Ed25519();
            List<String> verkeys = new ArrayList<>();
            verkeys.add(verkeyRecipient);
            String packedString = ed25519.packMessage(message, verkeys, verkeySender, sigkeySender);
            System.out.println(packedString);

            //String packedString = new String(packedBytes);

            UnpackModel unpackedModel = ed25519.unpackMessage(packedString, verkeyRecipient, sigkeyRecipient);
            System.out.println(message);
            System.out.println(verkeySender);
            System.out.println(verkeyRecipient);
            System.out.println("------");
            System.out.println(unpackedModel.getMessage());
            System.out.println(unpackedModel.getSender_vk());
            System.out.println(unpackedModel.getRecip_vk());

            //    assert message == unpacked
        } catch (SiriusCryptoError siriusCryptoError) {
            siriusCryptoError.printStackTrace();
        } catch (SodiumException e) {
            e.printStackTrace();
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }
        //  verkey, sigkey = create_keypair(b'000000000000000000000000000SEED1')
        //   verkey_recipient = bytes_to_b58(verkey)
        //  sigkey_recipient = bytes_to_b58(sigkey)
        //  verkey, sigkey = create_keypair(b'000000000000000000000000000SEED2')
        //   verkey_sender = bytes_to_b58(verkey)
        //  sigkey_sender = bytes_to_b58(sigkey)


/*
        packed = pack_message(
                message=message,
                to_verkeys=[verkey_recipient],
                from_verkey=verkey_sender,
                from_sigkey=sigkey_sender
    )
        unpacked, sender_vk, recip_vk = unpack_message(
                enc_message=packed,
                my_verkey=verkey_recipient,
                my_sigkey=sigkey_recipient
        )
        assert message == unpacked
        assert sender_vk, verkey_sender
        assert recip_vk, verkey_recipient*/
    }


    @Test
    public void test_fixture() {
        Custom custom = new Custom();
        KeyPair keyPairRecipient = null;
        try {
            keyPairRecipient = custom.createKeypair("000000000000000000000000000SEED1".getBytes(StandardCharsets.US_ASCII));

            String verkey_recipient = custom.bytesToB58(keyPairRecipient.getPublicKey().getAsBytes());
            String sigkey_recipient = custom.bytesToB58(keyPairRecipient.getSecretKey().getAsBytes());

            KeyPair keyPairSender = custom.createKeypair("000000000000000000000000000SEED2".getBytes(StandardCharsets.US_ASCII));
            String verkeySender = custom.bytesToB58(keyPairSender.getPublicKey().getAsBytes());
            String sigkeySender = custom.bytesToB58(keyPairSender.getSecretKey().getAsBytes());

            Ed25519 ed25519 = new Ed25519();
         //   List<String> verkeys = new ArrayList<>();
          //  verkeys.add(verkeyRecipient);

            String packed= "{\"protected\": \"eyJlbmMiOiAieGNoYWNoYTIwcG9seTEzMDVfaWV0ZiIsICJ0eXAiOiAiSldNLzEuMCIsICJhbGciOiAiQXV0aGNyeXB0IiwgInJlY2lwaWVudHMiOiBbeyJlbmNyeXB0ZWRfa2V5IjogInBKcW1xQS1IVWR6WTNWcFFTb2dySGx4WTgyRnc3Tl84YTFCSmtHU2VMT014VUlwT0RQWTZsMVVsaVVvOXFwS0giLCAiaGVhZGVyIjogeyJraWQiOiAiM1ZxZ2ZUcDZRNFZlRjhLWTdlVHVXRFZBWmFmRDJrVmNpb0R2NzZLR0xtZ0QiLCAic2VuZGVyIjogIjRlYzhBeFRHcWtxamd5NHlVdDF2a0poeWlYZlNUUHo1bTRKQjk1cGZSMG1JVW9KajAwWmswNmUyUEVDdUxJYmRDck8xeTM5LUhGTG5NdW5YQVJZWk5rZ2pyYV8wYTBQODJpbVdNcWNHc1FqaFd0QUhOcUw1OGNkUUYwYz0iLCAiaXYiOiAiVU1PM2o1ZHZwQnFMb2Rvd3V0c244WEMzTkVqSWJLb2oifX1dfQ==\", \"iv\": \"MchkHF2M-4hneeUJ\", \"ciphertext\": \"UgcdsV-0rIkP25eJuRSROOuqiTEXp4NToKjPMmqqtJs-Ih1b5t3EEbrrHxeSfPsHtlO6J4OqA1jc5uuD3aNssUyLug==\", \"tag\": \"sQD8qgJoTrRoyQKPeCSBlQ==\"}";

            UnpackModel unpackedModel = ed25519.unpackMessage(packed, verkey_recipient, sigkey_recipient);

           /* message = json.dumps({
                    'content':'Test encryption строка'
    })*/
            /*assert message == unpacked
            assert sender_vk,verkey_sender
            assert recip_vk,verkey_recipient*/
        } catch (SiriusCryptoError siriusCryptoError) {
            siriusCryptoError.printStackTrace();
        } catch (SodiumException e) {
            e.printStackTrace();
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }
    }


}
