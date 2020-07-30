package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

public class InsufficientFundsException extends IndyException {
    private static final long serialVersionUID = 6397499268992083528L;
    private static final String message = "Insufficient funds on inputs";

    /**
     * Initializes a new {@link InsufficientFundsException} with the specified message.
     */
    public InsufficientFundsException() {
        super(message, ErrorCode.InsufficientFundsError.value());
    }
}
