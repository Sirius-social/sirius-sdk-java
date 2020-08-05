package com.sirius.sdk.encryption;

public class EncryptModel {
    byte[] ciphertext;
    byte[] nonce;
    byte[] tag;

    public EncryptModel(byte[] ciphertext, byte[] nonce, byte[] tag) {
        this.ciphertext = ciphertext;
        this.nonce = nonce;
        this.tag = tag;
    }
}
