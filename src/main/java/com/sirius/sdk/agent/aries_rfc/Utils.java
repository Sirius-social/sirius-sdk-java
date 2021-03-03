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


    //async def sign(crypto: AbstractCrypto, value: Any, verkey: str, exclude_sig_data: bool = False) -> dict:
    //    timestamp_bytes = struct.pack(">Q", int(time.time()))
    //
    //    sig_data_bytes = timestamp_bytes + json.dumps(value).encode('ascii')
    //    sig_data = base64.urlsafe_b64encode(sig_data_bytes).decode('ascii')
    //
    //    signature_bytes = await crypto.crypto_sign(verkey, sig_data_bytes)
    //    signature = base64.urlsafe_b64encode(
    //        signature_bytes
    //    ).decode('ascii')
    //
    //    data = {
    //        "@type": "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/signature/1.0/ed25519Sha512_single",
    //        "signer": verkey,
    //        "signature": signature
    //    }
    //    if not exclude_sig_data:
    //        data['sig_data'] = sig_data
    //
    //    return data

    public static Pair<JSONObject, Boolean> verifySigned(AbstractCrypto crypto, JSONObject signed) {
        return new Pair<>(new JSONObject(), true);
    }
}
