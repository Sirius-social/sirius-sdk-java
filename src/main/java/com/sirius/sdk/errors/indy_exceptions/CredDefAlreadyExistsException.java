package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when an anoncreds accumulator is full.
 */
public class CredDefAlreadyExistsException extends IndyException
{
	private static final long serialVersionUID = -6792822612990030627L;
	private final static String message = "The anoncreds accumulator is full.";

	/**
	 * Initializes a new CredDefAlreadyExistsException.
	 */
	public CredDefAlreadyExistsException(IndyError error) {
		super(message + error.buildMessage(), ErrorCode.AnoncredsCredDefAlreadyExistsError.value());
	}
}