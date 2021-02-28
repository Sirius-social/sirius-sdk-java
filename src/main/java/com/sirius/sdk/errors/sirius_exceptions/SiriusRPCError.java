package com.sirius.sdk.errors.sirius_exceptions;

import com.sirius.sdk.errors.BaseSiriusException;

public class SiriusRPCError extends BaseSiriusException {

    public SiriusRPCError(String message) {
        super(message);
    }

    public SiriusRPCError() {
        super();
    }
}
