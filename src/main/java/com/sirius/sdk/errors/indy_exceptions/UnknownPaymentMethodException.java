package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when unknown (e.g. unregistered) payment method was called
 */
public class UnknownPaymentMethodException extends IndyException {

    private static final long serialVersionUID = -8226688236266389417L;
    private static final String MESSAGE = "An unknown payment method was called";

    /**
     * Initializes a new {@link UnknownPaymentMethodException}
     */
    public UnknownPaymentMethodException() {
        super(MESSAGE, ErrorCode.UnknownPaymentMethod.value());
    }
}
