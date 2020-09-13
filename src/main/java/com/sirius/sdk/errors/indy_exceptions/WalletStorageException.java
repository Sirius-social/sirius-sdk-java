package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown occurred during wallet operation.
 */
public class WalletStorageException extends IndyException
{
	private static final long serialVersionUID = 1829076830401150667L;
	private final static String message = "Storage error occurred during wallet operation.";

	/**
	 * Initializes a new WalletStorageException.
	 */
	public WalletStorageException(IndyError error)
	{
		super(message +error.buildMessage(), ErrorCode.WalletStorageError.value());
	}
}