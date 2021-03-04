package com.sirius.sdk.agent.aries_rfc;

import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.utils.Pair;
import com.sirius.sdk.utils.StringUtils;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class Utils {


    public static String utcToStr(Date date){
    //    dt.strftime('%Y-%m-%dT%H:%M:%S') + '+0000'
        return "";
    }

    public static JSONObject sign(AbstractCrypto crypto, JSONObject value, String verkey, boolean excludeSigData) {
        byte[] timestampBytes = ByteBuffer.allocate(8).putLong(System.currentTimeMillis() / 1000).array();
        byte[] sigDataBytes = ArrayUtils.addAll(timestampBytes, value.toString().getBytes(StandardCharsets.US_ASCII));
        String sigSata = new String(Base64.getUrlEncoder().encode((sigDataBytes)), StandardCharsets.US_ASCII);

        byte[] signatureBytes = crypto.cryptoSign(verkey, sigDataBytes);
        String signature = new String(Base64.getUrlEncoder().encode(signatureBytes), StandardCharsets.US_ASCII);

        JSONObject data = (new JSONObject()).
                put("@type", "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/signature/1.0/ed25519Sha512_single").
                put("signer", verkey).put("signature", signature);

        if (!excludeSigData) {
            data.put("sig_data", sigSata);
        }

        return data;
    }

    public static JSONObject sign(AbstractCrypto crypto, JSONObject value, String verkey) {
        return sign(crypto, value, verkey, false);
    }

    public static Pair<JSONObject, Boolean> verifySigned(AbstractCrypto crypto, JSONObject signed) {
        byte[] signatureBytes = Base64.getUrlEncoder().encode(signed.optString("signature").getBytes(StandardCharsets.US_ASCII));
        byte[] sigDataBytes = Base64.getUrlEncoder().encode(signed.optString("sig_data").getBytes(StandardCharsets.US_ASCII));
        boolean sigVerified = crypto.cryptoVerify(signed.optString("signer"), sigDataBytes, signatureBytes);
        byte[] dataBytes = Base64.getUrlDecoder().decode(signed.optString("sig_data"));
        JSONObject fieldJson = new JSONObject(
                new String(Arrays.copyOfRange(dataBytes, 8, dataBytes.length), StandardCharsets.US_ASCII));
        return new Pair<>(fieldJson, sigVerified);
    }
}
