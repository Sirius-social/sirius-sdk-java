package com.sirius.sdk.encryption;

import com.goterl.lazycode.lazysodium.exceptions.SodiumException;

import com.goterl.lazycode.lazysodium.interfaces.SecretBox;

import com.goterl.lazycode.lazysodium.interfaces.Sign;
import com.goterl.lazycode.lazysodium.utils.KeyPair;

import com.sirius.sdk.errors.sirius_exceptions.SiriusCryptoError;
import com.sirius.sdk.naclJava.LibSodium;
import com.sirius.sdk.utils.Base58;
import org.apache.commons.lang.ArrayUtils;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class Custom {

    /**
     * Convert a base 64 string to bytes.
     *
     * @param value   input base64 value
     * @param urlSafe flag if needed to convert to urlsafe presentation
     * @return bytes array
     */
    public static byte[] b64ToBytes(String value, boolean urlSafe) {
       byte[] valueBytes =  value.getBytes(StandardCharsets.US_ASCII);
     /*   if isinstance(value, str):
        value = value.encode('ascii')
        if urlsafe:
        missing_padding = len(value) % 4
        if missing_padding:
        value += b'=' * (4 - missing_padding)
        return base64.urlsafe_b64decode(value)
        return base64.b64decode(value)
*/
        byte[] encodedByte;
        if (urlSafe) {
            int missing_padding = valueBytes.length % 4;
            encodedByte = Base64.getUrlDecoder().decode(valueBytes);
        } else {
            encodedByte = Base64.getDecoder().decode(valueBytes);
        }
        return encodedByte;
    }

    /**
     * Convert a bytes to base 64 string.
     *
     * @param bytes   input bytes array
     * @param urlSafe flag if needed to convert to urlsafe presentation
     * @return base64 presentation
     */
    public static String bytesToB64(byte[] bytes, boolean urlSafe) {
        if(bytes == null) {
            return null;
        }
        byte[] decodedByte;
        if (urlSafe) {
            decodedByte = Base64.getUrlEncoder().encode(bytes);
        } else {
            decodedByte = Base64.getEncoder().encode(bytes);
        }
        return new String(decodedByte, StandardCharsets.US_ASCII);
    }


    /**
     * Convert a base 58 string to bytes.
     * <p>
     * Small cache provided for key conversions which happen frequently in pack
     * and unpack and message handling.
     */
    public static byte[] b58ToBytes(String value) {
        return Base58.decode(value);
    }

    /**
     * Convert a byte string to base 58.
     * Small cache provided for key conversions which happen frequently in pack
     * and unpack and message handling.
     */
    public static String bytesToB58(byte[] value) {
        return Base58.encode(value);
    }

    /**
     * Create a public and private signing keypair from a seed value.
     *
     * @param seed (bytes) Seed for keypair
     * @return A tuple of (public key, secret key)
     */
    public static KeyPair createKeypair(byte[] seed) throws SiriusCryptoError, SodiumException {
        //  Sodium.crypto_sign_seed_keypair()
        if (seed != null) {
            validateSeed(seed);
        } else {
            seed = randomSeed();
        }
        return LibSodium.getInstance().getLazySodium().cryptoSignSeedKeypair(seed);
    }

    /**
     * Generate a random seed value.
     *
     * @return A new random seed
     */
    public static byte[] randomSeed() {

        return LibSodium.getInstance().getLazySodium().randomBytesBuf(SecretBox.KEYBYTES);

        //   return new Random().randomBytes(Sodium.crypto_secretbox_keybytes());
    }

    /**
     * Convert a seed parameter to standard format and check length.
     *
     * @param message The seed to validate
     * @return The validated and encoded seed
     */
    public static byte[] validateSeed(String message) throws SiriusCryptoError {
        if (message == null) {
            return null;
        }
        byte[] bytes;
        if (message.contains("=")) {
            bytes = b64ToBytes(message, false);
        } else {
            bytes = message.getBytes(StandardCharsets.US_ASCII);
        }
        return validateSeed(bytes);
    }

    /**
     * Convert a seed parameter to standard format and check length.
     *
     * @param bytes The seed to validate
     * @return The validated and encoded seed
     */
    public static byte[] validateSeed(byte[] bytes) throws SiriusCryptoError {
        if (bytes == null) {
            return null;
        }
        if (bytes.length != 32) {
            throw new SiriusCryptoError("Seed value must be 32 bytes in length");
        }
        return bytes;
    }

    /**
     * Sign a message using a private signing key.
     * @param message The message to sign
     * @param secret The private signing key
     * @return The signature
     */
    public static byte[] signMessage(byte[] message, byte[] secret) {
        byte[] signedMessage = new byte[Sign.BYTES + message.length];
        if (LibSodium.getInstance().getLazySodium().cryptoSign(signedMessage, message, message.length, secret)) {
            return Arrays.copyOfRange(signedMessage, 0, Sign.BYTES);
        }
        return null;
    }

    /**
     * Verify a signed message according to a public verification key.
     * @param verkey The verkey to use in verification
     * @param message original message
     * @param signature signature
     * @return
     */
    public static boolean verifySignedMessage(byte[] verkey, byte[] message, byte[] signature) {
        byte[] signedMessage = ArrayUtils.addAll(signature, message);
        return LibSodium.getInstance().getLazySodium().cryptoSignOpen(message, signedMessage, signedMessage.length, verkey);
    }

    public static byte[] didFromVerkey(byte[] verkey) {
        return Arrays.copyOfRange(verkey, 0, 16);
    }

}
