package com.sirius.sdk.encryption;

import java.util.HashMap;
import java.util.Map;

public class P2PConnection {

    Map<String,String> myKeys;
    String theirVerKey;

    public P2PConnection(Map<String,String> myKeys, String theirVerKey) {
        this.myKeys = myKeys;
        this.theirVerKey = theirVerKey;

    }

  /**
     *  Encrypt message
     *
     * @param message
     * @return encrypted message
     */

    public byte[] pack(String message){
        //return new Ed25519().packMessage(message, theirVerKey,myKeys[0],myKeys[1] );
        return null;
    }

    /**
     *  Decrypt message
     *
     * @param bytes
     * @param dict enc_message: encoded message
     * @return decrypted message
     */
    public  Map<String,String> unpack(byte[] bytes , Map<String,String> dict){
        return new HashMap<String, String>();
    }



}
