package com.sirius.sdk.errors.sirius_exceptions;

public class SiriusPendingOperation extends RuntimeException {

    public SiriusPendingOperation() {
        super();
    }

    public SiriusPendingOperation(String message) {
        super(message);
    }
}
