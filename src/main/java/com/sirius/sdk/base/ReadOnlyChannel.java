package com.sirius.sdk.base;

import java.util.concurrent.CompletableFuture;

/**
 * Communication abstraction for reading data stream
 */
public interface ReadOnlyChannel {
    /**
     * Read message packet
     * @return chunk of data stream
     */
    CompletableFuture<byte[]> read();

}

