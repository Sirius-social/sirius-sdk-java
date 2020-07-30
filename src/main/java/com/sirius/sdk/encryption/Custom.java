package com.sirius.sdk.encryption;

import com.sirius.sdk.Main;
import com.sirius.sdk.utils.Base58;
import org.libsodium.jni.Sodium;
import org.libsodium.jni.crypto.Random;

import java.util.Base64;

public class Custom {

    /**
     * Convert a base 64 string to bytes.
     *
     * @param value input base64 value
     * @param urlSafe  flag if needed to convert to urlsafe presentation
     * @return bytes array
     */
    public byte[] b64ToBytes(String value, boolean urlSafe) {
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
        if(urlSafe){
            encodedByte = Base64.getUrlDecoder().decode(value);
        }else{
            encodedByte = Base64.getDecoder().decode(value);
        }
        return encodedByte;
    }

    /**
     * Convert a bytes to base 64 string.
     *
     * @param bytes input bytes array
     * @param urlSafe flag if needed to convert to urlsafe presentation
     * @return base64 presentation
     */
    public String bytesToB64(byte[] bytes, boolean urlSafe) {
        byte[] decodedByte;
        if(urlSafe){
            decodedByte = Base64.getUrlEncoder().encode(bytes);
        }else{
            decodedByte = Base64.getEncoder().encode(bytes);
        }
        return new String(decodedByte);
    }


    /**
     * Convert a base 58 string to bytes.
     * <p>
     * Small cache provided for key conversions which happen frequently in pack
     * and unpack and message handling.
     */
    public byte[] b58ToBytes(String value) {
        return Base58.decode(value);
    }

    /**
     * Convert a byte string to base 58.
     * Small cache provided for key conversions which happen frequently in pack
     * and unpack and message handling.
     */
    public String bytesToB58(byte[] value) {
        return Base58.encode(value);
    }


    public void createKeypair(byte[] seed) {
        //  Sodium.crypto_sign_seed_keypair()
    }
/*    def create_keypair(seed: bytes = None) -> (bytes, bytes):
            """
    Create a public and private signing keypair from a seed value.

    :param seed: (bytes) Seed for keypair
    :return A tuple of (public key, secret key)
    """
            if seed:
    validate_seed(seed)
    else:
    seed = random_seed()
    pk, sk = nacl.bindings.crypto_sign_seed_keypair(seed)
            return pk, sk*/


    /**
     * Generate a random seed value.
     *
     * @return A new random seed
     */
    public byte[] randomSeed() {
        return new Random().randomBytes(Sodium.crypto_secretbox_keybytes());
    }

    /**
     *   Convert a seed parameter to standard format and check length.
     * @param message  The seed to validate
     * @param bytes The seed to validate
     * @return  The validated and encoded seed
     */
    public byte[] validateSeed(String message,  byte[] bytes) {

        return null;
    }
 /*   def validate_seed(seed: Union[str, bytes]) -> Optional[bytes]:
 
            if not seed:
            return None
    if isinstance(seed, str):
            if "=" in seed:
    seed = b64_to_bytes(seed)
        else:
    seed = seed.encode("ascii")
            if not isinstance(seed, bytes):
    raise SiriusCryptoError("Seed value is not a string or bytes")
    if len(seed) != 32:
    raise SiriusCryptoError("Seed value must be 32 bytes in length")
    return seed*/

}
