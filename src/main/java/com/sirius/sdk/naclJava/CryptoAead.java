package com.sirius.sdk.naclJava;

import com.goterl.lazycode.lazysodium.LazySodium;
import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.interfaces.AEAD;
import com.goterl.lazycode.lazysodium.interfaces.Box;
import com.goterl.lazycode.lazysodium.interfaces.Sign;
import com.goterl.lazycode.lazysodium.utils.Key;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.errors.sirius_exceptions.SiriusFieldValueError;
import com.sirius.sdk.utils.Base58;


import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class CryptoAead {

    public byte[] decrypt(byte[] cipher, byte[] additionalData, byte[] nPub, Key k, com.goterl.lazycode.lazysodium.interfaces.AEAD.Method method) {
        return this.decrypt(cipher, additionalData, (byte[]) null, nPub, k, method);
    }

    public byte[] decrypt(byte[] cipher, byte[] additionalData, byte[] nSec, byte[] nPub, Key k, com.goterl.lazycode.lazysodium.interfaces.AEAD.Method method) {
        //byte[] cipherBytes = cipher.getBytes(StandardCharsets.US_ASCII);
        byte[] additionalDataBytes = additionalData == null ? new byte[0] : additionalData;
        long additionalBytesLen = additionalData == null ? 0L : (long) additionalDataBytes.length;
        byte[] keyBytes = k.getAsBytes();
        byte[] messageBytes;
        if (method.equals(com.goterl.lazycode.lazysodium.interfaces.AEAD.Method.CHACHA20_POLY1305_IETF)) {
            messageBytes = new byte[cipher.length - 16];
            long[] mlen = new long[messageBytes.length];
            LibSodium.getInstance().getNativeAaed().cryptoAeadChaCha20Poly1305IetfDecrypt(messageBytes, null, nSec, cipher, (long) cipher.length, additionalDataBytes, additionalBytesLen, nPub, keyBytes);
            return messageBytes;
        }
        return null;
    }



    public byte[] cryptoBoxSeal(byte[] messageBytes, Key publicKey) throws SodiumException {
        byte[] keyBytes = publicKey.getAsBytes();
        byte[] cipher = new byte[48 + messageBytes.length];
        if (!LibSodium.getInstance().getNativeBox().cryptoBoxSeal(cipher, messageBytes, (long)messageBytes.length, keyBytes)) {
            throw new SodiumException("Could not encrypt message.");
        } else {
            return cipher;
        }
    }
    public byte[] encrypt(String m, String additionalData, byte[] nPub, Key k, com.goterl.lazycode.lazysodium.interfaces.AEAD.Method method) {
        return this.encrypt(m, additionalData, (byte[])null, nPub, k, method);
    }

    public byte[] encrypt(String m, String additionalData, byte[] nSec, byte[] nPub, Key k, com.goterl.lazycode.lazysodium.interfaces.AEAD.Method method) {
        byte[] messageBytes = m.getBytes(StandardCharsets.US_ASCII);
        byte[] additionalDataBytes = additionalData == null ? new byte[0] : additionalData.getBytes(StandardCharsets.US_ASCII);
        long additionalBytesLen = additionalData == null ? 0L : (long)additionalDataBytes.length;
        byte[] keyBytes = k.getAsBytes();
        byte[] cipherBytes;
       if (method.equals(com.goterl.lazycode.lazysodium.interfaces.AEAD.Method.CHACHA20_POLY1305_IETF)) {
            cipherBytes = new byte[messageBytes.length + 16];
            LibSodium.getInstance().getLazySodium().cryptoAeadChaCha20Poly1305IetfEncrypt(cipherBytes, (long[])null, messageBytes, (long)messageBytes.length, additionalDataBytes, additionalBytesLen, nSec, nPub, keyBytes);
            return cipherBytes;
        }
       return null;
    }


    public byte[] cryptoBox(byte[] messageBytes, byte[] nonce, KeyPair keyPair) throws SodiumException {
        ByteArrayOutputStream bObj = new ByteArrayOutputStream();
        bObj.reset();
        byte[] cipherBytesPadding = new byte[32];
        for (byte cipherBytesPadding1 :cipherBytesPadding ){
            bObj.write(cipherBytesPadding1);
        }
        for (byte mesByte : messageBytes ){
            bObj.write(mesByte);
        }
        byte[] cipherBytes = new byte[32 + messageBytes.length];
        byte[] messageBytesPadded = bObj.toByteArray();
        boolean res = LibSodium.getInstance().getNativeBox().cryptoBox(cipherBytes, messageBytesPadded, (long)messageBytesPadded.length, nonce, keyPair.getPublicKey().getAsBytes(), keyPair.getSecretKey().getAsBytes());
        if (!res) {
            throw new SodiumException("Could not encrypt your message.");
        } else {
            ByteArrayOutputStream bObj2 = new ByteArrayOutputStream();
            bObj2.reset();
            int i=0;
            for (byte mesByte : cipherBytes ){
                if(i<=15){
                    i++;
                    continue;
                }
                bObj2.write(mesByte);

            }
            byte[] message16 = bObj2.toByteArray();
            return message16;

            //return new String(cipherBytes);
        }
    }

    public byte[] cryptoBoxOpen(byte[] cipherText, byte[] nonce, KeyPair keyPair) throws SodiumException {
        byte[] message = new byte[cipherText.length + 16];
        byte[] cipherBytesPadding = new byte[16];
        ByteArrayOutputStream bObj = new ByteArrayOutputStream();
        bObj.reset();

        for (byte cipherBytesPadding1 :cipherBytesPadding ){
            bObj.write(cipherBytesPadding1);
        }
        for (byte mesByte : cipherText ){
            bObj.write(mesByte);
        }
        byte[] padded = bObj.toByteArray();

        boolean res =  LibSodium.getInstance().getNativeBox().cryptoBoxOpen(message, padded, (long)padded.length, nonce, keyPair.getPublicKey().getAsBytes(), keyPair.getSecretKey().getAsBytes());
        if (!res) {
            throw new SodiumException("Could not decrypt your message.");
        } else {
           // new byte[32]{message}
            ByteArrayOutputStream bObj2 = new ByteArrayOutputStream();
            bObj2.reset();
            int i=0;
            for (byte mesByte : message ){
                if(i<=31){
                    i++;
                    continue;
                }
                bObj2.write(mesByte);

            }

            byte[] messageAfter32 = bObj2.toByteArray();
            return messageAfter32;
        }
    }

    public byte[] cryptoBoxSeal(String messageString, Key publicKey) throws SodiumException {
        byte[] keyBytes = publicKey.getAsBytes();
        byte[] message = messageString.getBytes(StandardCharsets.US_ASCII);
        int _mlen = message.length;
        int _clen = 48 + _mlen;
        byte[] ciphertext = new byte[_clen];
        if (!LibSodium.getInstance().getNativeBox().cryptoBoxSeal(ciphertext, message, (long)_mlen, keyBytes)) {
            throw new SodiumException("Could not encrypt message.");
        } else {
            return ciphertext;
        }
    }

    public String cryptoBoxSealOpen(String cipherString, KeyPair keyPair) throws SodiumException {
        byte[] cipherText = cipherString.getBytes(StandardCharsets.US_ASCII);
        int _clen = cipherText.length;
        int _mlen = _clen - 48;

        byte[] plaintext = new byte[_mlen];
        boolean res = LibSodium.getInstance().getNativeBox().cryptoBoxSealOpen(plaintext, cipherText, (long)_clen, keyPair.getPublicKey().getAsBytes(), keyPair.getSecretKey().getAsBytes());
        if (!res) {
            throw new SodiumException("Could not decrypt your message.");
        } else {
            return new String(plaintext,StandardCharsets.US_ASCII);
        }
    }

    public byte[] cryptoBoxSealOpen(byte[] cipherText, KeyPair keyPair) throws SodiumException {
       // byte[] cipherText = cipherString.getBytes(StandardCharsets.US_ASCII);
        int _clen = cipherText.length;
        int _mlen = _clen - 48;

        byte[] plaintext = new byte[_mlen];
        boolean res = LibSodium.getInstance().getNativeBox().cryptoBoxSealOpen(plaintext, cipherText, (long)_clen, keyPair.getPublicKey().getAsBytes(), keyPair.getSecretKey().getAsBytes());
        if (!res) {
            throw new SodiumException("Could not decrypt your message.");
        } else {
            return plaintext;
        }
    }


}