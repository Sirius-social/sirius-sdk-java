package com.sirius.sdk.utils;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.sirius.sdk.naclJava.LibSodium;
import org.bitcoinj.core.Base58;

public class IotaUtils {
    public static String generateTag(byte[] key) {
        LazySodiumJava s = LibSodium.getInstance().getLazySodium();
        byte[] outputBytes = new byte[32];
        s.cryptoGenericHash(outputBytes, 32, key, key.length, null, 0);
        return Base58.encode(outputBytes);
    }
}
