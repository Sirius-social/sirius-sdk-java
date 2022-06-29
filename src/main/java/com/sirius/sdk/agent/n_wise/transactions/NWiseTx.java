package com.sirius.sdk.agent.n_wise.transactions;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.interfaces.Hash;
import com.sirius.sdk.naclJava.LibSodium;
import org.bitcoinj.core.Base58;
import org.json.JSONObject;

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
}
