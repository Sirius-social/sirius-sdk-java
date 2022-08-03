package com.sirius.sdk.base;

import io.reactivex.rxjava3.core.Observable;

import java.util.concurrent.CompletableFuture;

/**
 * Communication abstraction for reading data stream
 */
public interface ReadOnlyChannel {

    public Observable<byte[]> listen();

}

