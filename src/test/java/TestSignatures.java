import com.sirius.sdk.hub.Context;
import com.sirius.sdk.utils.JcsEd25519Signature2020LdSigner;
import com.sirius.sdk.utils.JcsEd25519Signature2020LdVerifier;
import helpers.ConfTest;
import helpers.ServerTestSuite;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;

public class TestSignatures {

    ConfTest confTest;

    String testEd25519PublicKeyString =
            "de8777a28f8da1a74e7a13090ed974d879bf692d001cddee16e4cc9f84b60580";
    byte[] testEd25519PublicKey = Hex.decodeHex(testEd25519PublicKeyString.toCharArray());

    public TestSignatures() throws DecoderException {
    }

    @Before
    public void configureTest() {
        confTest = ConfTest.newInstance();
    }

    @Test
    public void testLdVerify() throws DecoderException, IOException {
        JSONObject jsonDocGood = new JSONObject(new String(TestSignatures.class.getResourceAsStream("signed.good.JcsEd25519Signature2020.jsonld").readAllBytes()));
        JcsEd25519Signature2020LdVerifier verifier = new JcsEd25519Signature2020LdVerifier(testEd25519PublicKey);
        Assert.assertTrue(verifier.verify(jsonDocGood));

        JSONObject jsonDocBad = new JSONObject(new String(TestSignatures.class.getResourceAsStream("signed.bad.JcsEd25519Signature2020.jsonld").readAllBytes()));
        Assert.assertFalse(verifier.verify(jsonDocBad));
    }
}
