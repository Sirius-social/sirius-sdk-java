package com.sirius.sdk.naclJava;

import com.goterl.lazycode.lazysodium.LazySodiumJava;
import com.goterl.lazycode.lazysodium.SodiumJava;
import com.goterl.lazycode.lazysodium.interfaces.*;

import java.nio.charset.StandardCharsets;

public class LibSodium {

    private static volatile LibSodium mInstance;

    public LazySodiumJava getLazySodium() {
        return lazySodium;
    }

    public AEAD.Lazy getLazyAaed() {
        return (AEAD.Lazy) getLazySodium();
    }
    public AEAD.Native getNativeAaed() {
        return (AEAD.Native) getLazySodium();
    }

    public Box.Lazy getLazyBox() {
        return (Box.Lazy) getLazySodium();
    }
    public Box.Native getNativeBox() {
        return (Box.Native) getLazySodium();
    }

    public Auth.Lazy getLazyAuth() {
        return (Auth.Lazy) getLazySodium();
    }
    public Hash.Lazy getLazyHash() {
        return (Hash.Lazy) getLazySodium();
    }
    public Sign.Lazy getLazySign() {
        return (Sign.Lazy) getLazySodium();
    }
    public PwHash.Lazy getLazyPwHash() {
        return (PwHash.Lazy) getLazySodium();
    }

    public Scrypt.Lazy getLazyScrypt() {
        return (Scrypt.Lazy) getLazySodium();
    }
    public Stream.Lazy getLazyStream() {
        return (Stream.Lazy) getLazySodium();
    }
    public Helpers.Lazy getLazyHelpers() {
        return (Helpers.Lazy) getLazySodium();
    }
    public Padding.Lazy getLazyPadding() {
        return (Padding.Lazy) getLazySodium();
    }
    public SecretBox.Lazy getLazySecretBox() {
        return (SecretBox.Lazy) getLazySodium();
    }
    public ShortHash.Lazy getLazyShortHash() {
        return (ShortHash.Lazy) getLazySodium();
    }
    public StreamJava.Lazy getLazyStreamJava() {
        return (StreamJava.Lazy) getLazySodium();
    }
    public GenericHash.Lazy getLazyGenericHash() {
        return (GenericHash.Lazy) getLazySodium();
    }
    public KeyExchange.Lazy getLazyKeyExchange() {
        return (KeyExchange.Lazy) getLazySodium();
    }
    public SecretStream.Lazy getLazySecretStream() {
        return (SecretStream.Lazy) getLazySodium();
    }
    public SecureMemory.Lazy getLazySecureMemory() {
        return (SecureMemory.Lazy) getLazySodium();
    }
    public DiffieHellman.Lazy getLazyDiffieHellman() {
        return (DiffieHellman.Lazy) getLazySodium();
    }
    public KeyDerivation.Lazy getLazyKeyDerivation() {
        return (KeyDerivation.Lazy) getLazySodium();
    }
    private LazySodiumJava lazySodium;

    private LibSodium() {
        lazySodium = new LazySodiumJava(new SodiumJava(), StandardCharsets.US_ASCII);
    }

    public static LibSodium getInstance() {
        if (mInstance == null) {
            synchronized (LibSodium.class) {
                if (mInstance == null) {
                    mInstance = new LibSodium();
                }
            }
        }
        return mInstance;
    }
}
