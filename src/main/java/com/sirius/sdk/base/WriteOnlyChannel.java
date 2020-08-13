package com.sirius.sdk.base;

/**
 * Communication abstraction for writing data stream
 */
public interface WriteOnlyChannel {
    /**
     *  Write message packet
     * @param data message packet
     * @return  True if success ele False
     */
    boolean write(byte[] data );
}
