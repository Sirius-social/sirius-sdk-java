package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempting to use a credential that has been revoked.
 */
public class CredentialRevokedException extends IndyException
{
	private static final long serialVersionUID = 8269746965241515882L;
	private final static String message = "The credential has been revoked.";

	/**
	 * Initializes a new CredentialRevokedException.
	 */
	public CredentialRevokedException() {
		super(message, ErrorCode.AnoncredsCredentialRevoked.value());
	}
}