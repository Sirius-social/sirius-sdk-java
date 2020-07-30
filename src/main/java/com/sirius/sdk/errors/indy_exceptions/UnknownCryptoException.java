package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempting to use a crypto format unrecognized by the SDK.
 */
public class UnknownCryptoException extends IndyException
{
	private static final long serialVersionUID = 4955846571270561834L;
	private final static String message = "An unknown crypto format has been used for a DID entity key.";

	/**
	 * Initializes a new UnknownCryptoException.
	 */
	public UnknownCryptoException() {
		super(message, ErrorCode.UnknownCryptoTypeError.value());
	}
}