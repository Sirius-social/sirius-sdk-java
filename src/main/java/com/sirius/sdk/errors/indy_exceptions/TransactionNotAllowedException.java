package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

public class TransactionNotAllowedException extends IndyException {
    private static final long serialVersionUID = 6397499268992083529L;
    private static final String message = "The transaction is not allowed to a requester";

    /**
     * Initializes a new {@link TransactionNotAllowedException} with the specified message.
     */
    public TransactionNotAllowedException() {
        super(message, ErrorCode.TransactionNotAllowedError.value());
    }
}
