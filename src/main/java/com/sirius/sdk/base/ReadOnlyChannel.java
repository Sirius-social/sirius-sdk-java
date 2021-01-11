package com.sirius.sdk.base;

import java.util.concurrent.CompletableFuture;

/**
 * Communication abstraction for reading data stream
 */
public interface ReadOnlyChannel {
    /**
     * Read message packet
     * @param timeout Operation timeout is sec
     * @return chunk of data stream
     */
    CompletableFuture<byte[]> read();

}

