package com.sirius.sdk.encryption;

public class DecryptModel {

    public DecryptModel(String cek,String sender_vk,  String recip_vk_b58) {
        this.sender_vk = sender_vk;
        this.cek = cek;
        this.recip_vk_b58 = recip_vk_b58;
    }

    public String getSender_vk() {
        return sender_vk;
    }

    public String getCek() {
        return cek;
    }

    public String getRecip_vk_b58() {
        return recip_vk_b58;
    }

    String sender_vk ;
    String cek ;
    String recip_vk_b58;
}
