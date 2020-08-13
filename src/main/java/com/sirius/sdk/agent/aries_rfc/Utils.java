package com.sirius.sdk.agent.aries_rfc;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Utils {
    public void sign(){

    }

    public void verifySigned(String signedMess ){
        JSONObject signedJsonObject = new JSONObject(signedMess);
        String signature = signedJsonObject.getString("signature");
        String sigData = signedJsonObject.getString("sig_data");
        byte[]signatureByte =  Base64.getUrlEncoder().encode(signature.getBytes(StandardCharsets.US_ASCII));
        byte[]sigDataByte =  Base64.getUrlEncoder().encode(sigData.getBytes(StandardCharsets.US_ASCII));
    }
}



/*
    async def sign(crypto: AbstractCrypto, value: Any, verkey: str) -> dict:
        timestamp_bytes = struct.pack(">Q", int(time.time()))

        sig_data_bytes = timestamp_bytes + json.dumps(value).encode('ascii')
        sig_data = base64.urlsafe_b64encode(sig_data_bytes).decode('ascii')

        signature_bytes = await crypto.crypto_sign(verkey, sig_data_bytes)
        signature = base64.urlsafe_b64encode(
        signature_bytes
        ).decode('ascii')

        return {
        "@type": "did:sov:BzCbsNYhMrjHiqZDTUASHg;spec/signature/1.0/ed25519Sha512_single",
        "signer": verkey,
        "sig_data": sig_data,
        "signature": signature
        }


        async def verify_signed(crypto: AbstractCrypto, signed: dict) -> (Any, bool):
        signature_bytes = base64.urlsafe_b64decode(signed['signature'].encode('ascii'))
        sig_data_bytes = base64.urlsafe_b64decode(signed['sig_data'].encode('ascii'))
        sig_verified = await crypto.crypto_verify(
        signed['signer'],
        sig_data_bytes,
        signature_bytes
        )
        data_bytes = base64.urlsafe_b64decode(signed['sig_data'])
        timestamp = struct.unpack(">Q", data_bytes[:8])
        field_json = data_bytes[8:]
        if isinstance(field_json, bytes):
        field_json = field_json.decode('utf-8')
        return json.loads(field_json), sig_verified*/
