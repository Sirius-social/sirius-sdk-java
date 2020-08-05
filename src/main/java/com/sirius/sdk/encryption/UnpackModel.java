package com.sirius.sdk.encryption;

public class UnpackModel {
   String message;

    public UnpackModel(String message, String sender_vk, String recip_vk) {
        this.message = message;
        this.sender_vk = sender_vk;
        this.recip_vk = recip_vk;
    }

    public String getMessage() {
        return message;
    }

    public String getSender_vk() {
        return sender_vk;
    }

    public String getRecip_vk() {
        return recip_vk;
    }

    String sender_vk;
   String recip_vk;
}
