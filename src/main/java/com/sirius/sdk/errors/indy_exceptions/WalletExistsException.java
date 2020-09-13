package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempting to create a wallet using the same name as a wallet that already exists.
 */
public class WalletExistsException extends IndyException
{
	private static final long serialVersionUID = 1829076830401150667L;
	private final static String message = "A wallet with the specified name already exists.";

	/**
	 * Initializes a new WalletExistsException.
	 */
	public WalletExistsException(IndyError error)
	{
		super(message+error.buildMessage(), ErrorCode.WalletAlreadyExistsError.value());
	}
}