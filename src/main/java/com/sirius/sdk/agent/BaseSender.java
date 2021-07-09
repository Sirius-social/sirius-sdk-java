package com.sirius.sdk.agent;

public abstract class BaseSender {
    public abstract boolean sendTo(String endpoint, byte[] data);
}
