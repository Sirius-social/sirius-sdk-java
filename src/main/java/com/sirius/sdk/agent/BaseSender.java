package com.sirius.sdk.agent;

public abstract class BaseSender {
    public abstract boolean sendTo(String endpoint, byte[] data);

    public abstract void open(String endpoint);
    public abstract void close();
}
