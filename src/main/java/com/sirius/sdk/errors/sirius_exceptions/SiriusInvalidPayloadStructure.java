package com.sirius.sdk.errors.sirius_exceptions;

import com.sirius.sdk.errors.BaseSiriusException;

public class SiriusInvalidPayloadStructure extends BaseSiriusException {
    public SiriusInvalidPayloadStructure(String message) {
        super(message);
    }
}
