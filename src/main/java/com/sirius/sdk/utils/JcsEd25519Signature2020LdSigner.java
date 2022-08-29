package com.sirius.sdk.utils;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.interfaces.Sign;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.naclJava.LibSodium;
import org.bitcoinj.core.Base58;
import org.erdtman.jcs.JsonCanonicalizer;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JcsEd25519Signature2020LdSigner {

    URI verificationMethod;

    public JcsEd25519Signature2020LdSigner() {
    }

    public void setVerificationMethod(URI verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public void sign(JSONObject jsonDoc, byte[] publicKey, AbstractCrypto crypto) {
        if (jsonDoc.has("proof"))
            jsonDoc.remove("proof");

        JSONObject proof = new JSONObject().
                put("type", "JcsEd25519Signature2020");

        if (this.verificationMethod != null)
            proof.put("verificationMethod", this.verificationMethod.toString());

        jsonDoc.put("proof", proof);

        try {
            JsonCanonicalizer jc = new JsonCanonicalizer(jsonDoc.toString());
            String canonicalized = jc.getEncodedString();
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonicalized.getBytes());
            byte[] signature = crypto.cryptoSign(Base58.encode(publicKey), digest);
            proof.put("signatureValue", Base58.encode(signature));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void sign(JSONObject jsonDoc, byte[] privateKey) {
        if (jsonDoc.has("proof"))
            jsonDoc.remove("proof");

        JSONObject proof = new JSONObject().
                put("type", "JcsEd25519Signature2020");

        if (this.verificationMethod != null)
                proof.put("verificationMethod", this.verificationMethod.toString());

        jsonDoc.put("proof", proof);

        try {
            JsonCanonicalizer jc = new JsonCanonicalizer(jsonDoc.toString());
            String canonicalized = jc.getEncodedString();
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonicalized.getBytes());
            LazySodiumJava s = LibSodium.getInstance().getLazySodium();
            byte[] signature = new byte[Sign.BYTES];
            s.cryptoSignDetached(signature, digest, digest.length, privateKey);
            proof.put("signatureValue", Base58.encode(signature));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
