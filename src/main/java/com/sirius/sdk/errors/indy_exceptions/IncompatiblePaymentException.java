package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception is thrown when information is incompatible e.g. 2 different payment methods in inputs and outputs
 */
public class IncompatiblePaymentException extends IndyException {

    private static final long serialVersionUID = 5531031012103688872L;
    private static final String message = "Information passed to libindy is incompatible";

    /**
     * Initializes a new {@link IncompatiblePaymentException} with the specified message.
     */
    public IncompatiblePaymentException(IndyError error) {
        super(message + error.buildMessage(), ErrorCode.IncompatiblePaymentError.value());
    }
}
