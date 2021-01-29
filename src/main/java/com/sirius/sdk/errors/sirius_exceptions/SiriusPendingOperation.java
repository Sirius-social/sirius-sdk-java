package com.sirius.sdk.errors.sirius_exceptions;

import com.sirius.sdk.errors.BaseSiriusException;

public class SiriusPendingOperation extends BaseSiriusException {

    public SiriusPendingOperation() {
        super();
    }

    public SiriusPendingOperation(String message) {
        super(message);
    }
}
