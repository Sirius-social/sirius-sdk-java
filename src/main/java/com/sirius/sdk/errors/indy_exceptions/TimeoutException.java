package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

import java.io.Serializable;

/**
 * Exception thrown when timeout happens for ledger operation.
 */
public class TimeoutException extends IndyException implements Serializable {
    private static final long serialVersionUID = -2318833884012610163L;
    private final static String message = "Timeout happens for ledger operation.";

    /**
     * Initializes a new TimeoutException.
     */
    public TimeoutException() {
        super(message, ErrorCode.PoolLedgerTimeout.value());
    }
}
