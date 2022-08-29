package com.sirius.sdk.utils;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.naclJava.LibSodium;
import org.erdtman.jcs.JsonCanonicalizer;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JcsEd25519Signature2020LdVerifier {

    byte[] publicKey;

    public JcsEd25519Signature2020LdVerifier(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public boolean verify(JSONObject jsonDoc) {
        if (!jsonDoc.has("proof") || !jsonDoc.getJSONObject("proof").has("signatureValue"))
            return false;
        JSONObject jsonDocCopy =  new JSONObject(jsonDoc.toString());
        byte[] signature = Base58.decode(jsonDoc.getJSONObject("proof").getString("signatureValue"));
        jsonDocCopy.getJSONObject("proof").remove("signatureValue");

        JsonCanonicalizer canonicalizer = null;
        try {
            canonicalizer = new JsonCanonicalizer(jsonDocCopy.toString());
            String canonicalized = canonicalizer.getEncodedString();
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonicalized.getBytes());

            LazySodiumJava s = LibSodium.getInstance().getLazySodium();
            return s.cryptoSignVerifyDetached(signature, digest, digest.length, this.publicKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
