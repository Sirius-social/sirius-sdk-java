package com.sirius.sdk.errors.sirius_exceptions;

import com.sirius.sdk.errors.BaseSiriusException;

public class SiriusValidationError extends BaseSiriusException {

    public SiriusValidationError(String message) {
        super(message);
    }
}
