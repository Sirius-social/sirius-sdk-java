package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when a invalid user revocation index is used.
 */
public class AnoncredsInvalidUserRevocId extends IndyException
{
	private static final long serialVersionUID = 4969718227042210813L;
	private final static String message = "The user revocation registry index specified is invalid.";

	/**
	 * Initializes a new AnoncredsInvalidUserRevocId.
	 */
	public AnoncredsInvalidUserRevocId(IndyError error)
	{
		super(message + error.buildMessage(), ErrorCode.AnoncredsInvalidUserRevocId.value());
	}
}