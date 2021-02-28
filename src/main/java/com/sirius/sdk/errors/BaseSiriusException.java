package com.sirius.sdk.errors;

public class BaseSiriusException extends Exception{

    /**
     * Initializes a new SiriusException with the specified message.
     *
     * @param message The message for the exception.
     */
    protected BaseSiriusException(String message) {
        super(message);
    }

    /**
     * Initializes a new SiriusException.
     */
    protected BaseSiriusException() {

    }


}
