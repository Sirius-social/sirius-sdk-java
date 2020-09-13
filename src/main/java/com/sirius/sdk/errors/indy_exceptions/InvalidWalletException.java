package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempting to use a wallet that has been closed.
 */
public class InvalidWalletException extends IndyException
{
	private static final long serialVersionUID = -606730416804502147L;
	private final static String message = "The wallet is closed or invalid and cannot be used.";

	/**
	 * Initializes a new WalletClosedException.
	 */
	public InvalidWalletException(IndyError error) {
		super(message + error.buildMessage(), ErrorCode.WalletInvalidHandle.value());
	}
}
