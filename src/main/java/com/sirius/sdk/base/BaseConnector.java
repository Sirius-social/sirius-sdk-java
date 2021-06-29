package com.sirius.sdk.base;


/**
 * Transport Layer.
 *
 *Connectors operate as transport provider for high-level abstractions
 */
public abstract class BaseConnector implements ReadOnlyChannel, WriteOnlyChannel {

    /**
     * Open communication
     */
    public abstract void open();
    /**
     * Close communication
     */
    public abstract void close();

    public abstract  boolean isOpen();
}
