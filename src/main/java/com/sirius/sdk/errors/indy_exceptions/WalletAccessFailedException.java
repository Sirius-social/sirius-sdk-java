package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Attempt to open encrypted wallet with invalid credentials
 */
public class WalletAccessFailedException extends IndyException
{
	private static final long serialVersionUID = 3294831240096535507L;
	private final static String message = "The wallet security error.";

	/**
	 * Initializes a new WalletAccessFailedException.
	 */
	public WalletAccessFailedException(IndyError error) {
		super(message  +error.buildMessage(), ErrorCode.WalletAccessFailed.value());
	}
}