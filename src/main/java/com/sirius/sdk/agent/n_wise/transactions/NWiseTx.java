package com.sirius.sdk.agent.n_wise.transactions;

import com.danubetech.keyformats.crypto.ByteSigner;
import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.interfaces.Hash;
import com.sirius.sdk.agent.wallet.abstract_wallet.AbstractCrypto;
import com.sirius.sdk.encryption.IndyWalletSigner;
import com.sirius.sdk.naclJava.LibSodium;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.signer.JcsEd25519Signature2020LdSigner;
import info.weboftrust.ldsignatures.signer.LdSigner;
import info.weboftrust.ldsignatures.suites.JcsEd25519Signature2020SignatureSuite;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class NWiseTx extends JSONObject {

    public NWiseTx() {
        super();
    }

    public NWiseTx(String str) {
        super(str);
    }

    public byte[] getPreviousTxHash() {
        String hash = optString("previousTxHashBase58");
        if (!hash.isEmpty()) {
            return Base58.decode(hash);
        }
        return new byte[0];
    }

    public String getPreviousTxHashBase58() {
        return optString("previousTxHashBase58");
    }

    public byte[] getHash() {
        LazySodiumJava s = LibSodium.getInstance().getLazySodium();
        byte[] inputBytes = toString().getBytes(StandardCharsets.UTF_8);
        byte[] outputBytes = new byte[Hash.SHA256_BYTES];
        s.cryptoHashSha256(outputBytes, inputBytes, inputBytes.length);
        return outputBytes;
    }

    public void sign(AbstractCrypto crypto, String did, byte[] verkey) {
        if (has("proof"))
            remove("proof");

        ByteSigner byteSigner = new IndyWalletSigner(crypto, Base58.encode(verkey));
        LdSigner<JcsEd25519Signature2020SignatureSuite> ldSigner = new JcsEd25519Signature2020LdSigner(byteSigner);
        ldSigner.setVerificationMethod(URI.create(did + "#1"));
        JsonLDObject jsonLdObject = JsonLDObject.fromJson(this.toString());
        try {
            JSONObject proof = new JSONObject(ldSigner.sign(jsonLdObject).toJson());
            put("proof", proof);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sign(String id, byte[] privateKey) {
        if (has("proof"))
            remove("proof");

        LdSigner<JcsEd25519Signature2020SignatureSuite> ldSigner = new JcsEd25519Signature2020LdSigner(privateKey);
        ldSigner.setVerificationMethod(URI.create(id));
        JsonLDObject jsonLdObject = JsonLDObject.fromJson(this.toString());
        try {
            JSONObject proof = new JSONObject(ldSigner.sign(jsonLdObject).toJson());
            put("proof", proof);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
