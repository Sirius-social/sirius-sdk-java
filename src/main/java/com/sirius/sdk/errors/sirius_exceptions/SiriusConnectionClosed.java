package com.sirius.sdk.errors.sirius_exceptions;

import com.sirius.sdk.errors.BaseSiriusException;

public class SiriusConnectionClosed extends BaseSiriusException {
    public SiriusConnectionClosed(String message) {
        super(message);
    }
}
