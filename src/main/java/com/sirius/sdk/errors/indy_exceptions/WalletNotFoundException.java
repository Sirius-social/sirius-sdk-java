package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when requesting a value from a wallet that does not contain the specified key.
 */
public class WalletNotFoundException extends IndyException
{
	private static final long serialVersionUID = 667964860056778208L;
	private final static String message = "No value with the specified key exists in the wallet from which it was requested.";

	/**
	 * Initializes a new WalletNotFoundException.
	 */
	public WalletNotFoundException(IndyError error)
	{
		super(message+error.buildMessage(), ErrorCode.WalletNotFoundError.value());
	}
}
