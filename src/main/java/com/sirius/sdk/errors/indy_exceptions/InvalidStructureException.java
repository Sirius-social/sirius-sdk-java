package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when a value passed to the SDK was not structured so that the SDK could correctly process it.
 */
public class InvalidStructureException extends IndyException
{
	private static final long serialVersionUID = -2157029980107821313L;
	private final static String message = "A value being processed is not valid.";

	/**
	 * Initializes a new InvalidStructureException.
	 */
	public InvalidStructureException(IndyError error)
	{
		super(message +error.buildMessage(), ErrorCode.CommonInvalidStructure.value());
	}

}
