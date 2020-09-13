package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown occurred during encryption-related operations.
 */
public class WalletEncryptionException extends IndyException
{
	private static final long serialVersionUID = 1829076830401150667L;
	private final static String message = "Error during encryption-related operations.";

	/**
	 * Initializes a new WalletEncryptionException.
	 */
	public WalletEncryptionException(IndyError error)
	{
		super(message + error.buildMessage(), ErrorCode.WalletEncryptionError.value());
	}
}