package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

public class ExtraFundsException extends IndyException {
    private static final long serialVersionUID = 6397499268992083529L;
    private static final String message = "Extra funds on inputs";

    /**
     * Initializes a new {@link ExtraFundsException} with the specified message.
     */
    public ExtraFundsException() {
        super(message, ErrorCode.ExtraFundsError.value());
    }
}
