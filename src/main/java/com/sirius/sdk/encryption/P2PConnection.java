package com.sirius.sdk.encryption;

import com.goterl.lazycode.lazysodium.exceptions.SodiumException;
import com.goterl.lazycode.lazysodium.utils.KeyPair;
import com.sirius.sdk.errors.sirius_exceptions.SiriusCryptoError;
import com.sirius.sdk.errors.sirius_exceptions.SiriusInvalidType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pairwise static connection compatible with Indy SDK
 */
public class P2PConnection {

    String fromVerkey;
    String fromSigKey;
    String theirVerKey;
    Ed25519 ed25519;
    /**
     * @param fromVerkey  verkey for encrypt/decrypt operations
     * @param fromSigKey  sigkey for encrypt/decrypt operations
     * @param theirVerKey their_verkey: verkey of the counterparty
     */
    public P2PConnection(String fromVerkey, String fromSigKey, String theirVerKey) {
        this.fromVerkey = fromVerkey;
        this.fromSigKey = fromSigKey;
        this.theirVerKey = theirVerKey;
        ed25519 =  new Ed25519();

    }

    /**
     * Encrypt message
     *
     * @param message
     * @return encrypted message
     */

    public String pack(String message) {
        List<String> toVerKeys = new ArrayList<>();
        toVerKeys.add(theirVerKey);
        try {
            return ed25519.packMessage(message, toVerKeys, fromVerkey, fromSigKey);
        } catch (SiriusCryptoError siriusCryptoError) {
            siriusCryptoError.printStackTrace();
        } catch (SodiumException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt message
     *
     * @param encMessage   encoded message
     * @return decrypted message
     */
    public String unpack(String encMessage) {
        try {
            UnpackModel unpackModel = ed25519.unpackMessage(encMessage,fromVerkey,fromSigKey);
            return unpackModel.getMessage();
        } catch (SiriusInvalidType siriusInvalidType) {
            siriusInvalidType.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
