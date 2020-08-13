package com.sirius.sdk.base;

/**
 * Communication abstraction for reading data stream
 */
public interface ReadOnlyChannel {
    /**
     * Read message packet
     * @param timeout Operation timeout is sec
     * @return chunk of data stream
     */
    byte[] read(int timeout);

}

