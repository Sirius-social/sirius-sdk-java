package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempting to open a wallet that has already been opened.
 */
public class WalletAlreadyOpenedException extends IndyException
{
	private static final long serialVersionUID = 3294831240096535507L;
	private final static String message = "The wallet is already open.";

	/**
	 * Initializes a new WalletAlreadyOpenedException.
	 */
	public WalletAlreadyOpenedException(IndyError error)
	{
		super(message +error.buildMessage(), ErrorCode.WalletAlreadyOpenedError.value());
	}
}