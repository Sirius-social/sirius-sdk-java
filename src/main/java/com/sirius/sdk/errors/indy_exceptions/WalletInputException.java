package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when input provided to wallet operations is considered not valid.
 */
public class WalletInputException extends IndyException
{
	private static final long serialVersionUID = 1829076830401150667L;
	private final static String message = "Input provided to wallet operations is considered not valid.";

	/**
	 * Initializes a new WalletInputException.
	 */
	public WalletInputException()
	{
		super(message, ErrorCode.WalletInputError.value());
	}
}