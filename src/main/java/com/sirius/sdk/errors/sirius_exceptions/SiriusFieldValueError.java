package com.sirius.sdk.errors.sirius_exceptions;

import com.sirius.sdk.errors.BaseSiriusException;

public class SiriusFieldValueError extends BaseSiriusException {
    public SiriusFieldValueError(String message) {
        super(message);
    }

    public SiriusFieldValueError() {
    }
}
