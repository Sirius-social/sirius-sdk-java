package com.sirius.sdk.encryption;

public class DecryptModel {

    public DecryptModel(byte[] cek,byte[] sender_vk,  String recip_vk_b58) {
        this.sender_vk = sender_vk;
        this.cek = cek;
        this.recip_vk_b58 = recip_vk_b58;
    }

    public byte[] getSender_vk() {
        return sender_vk;
    }

    public byte[] getCek() {
        return cek;
    }

    public String getRecip_vk_b58() {
        return recip_vk_b58;
    }

    byte[] sender_vk ;
    byte[] cek ;
    String recip_vk_b58;
}
