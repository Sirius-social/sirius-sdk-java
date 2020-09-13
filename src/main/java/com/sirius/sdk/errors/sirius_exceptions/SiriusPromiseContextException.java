package com.sirius.sdk.errors.sirius_exceptions;

import com.sirius.sdk.errors.BaseSiriusException;

public class SiriusPromiseContextException extends BaseSiriusException {

    public SiriusPromiseContextException(String className, String printable,String message) {
        super(message);
        this.className = className;
        this.printable = printable;
    }

    public SiriusPromiseContextException(String className, String printable) {
        super( String.join(";",className,printable));
        this.className = className;
        this.printable = printable;
    }
    String className;
    String printable;

}
