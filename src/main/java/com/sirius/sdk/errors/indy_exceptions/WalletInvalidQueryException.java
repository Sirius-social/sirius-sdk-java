package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when provided wallet query is invalid.
 */
public class WalletInvalidQueryException extends IndyException
{
	private static final long serialVersionUID = 667964860056778208L;
	private final static String message = "Wallet query is invalid.";

	/**
	 * Initializes a new WalletInvalidQueryException.
	 */
	public WalletInvalidQueryException(IndyError error)
	{
		super(message + error.buildMessage(), ErrorCode.WalletQueryError.value());
	}
}
