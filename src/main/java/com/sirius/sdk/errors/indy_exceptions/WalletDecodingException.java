package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when decoding of wallet data during input/output failed.
 */
public class WalletDecodingException extends IndyException
{
	private static final long serialVersionUID = 1829076830401150667L;
	private final static String message = "Decoding of wallet data during input/output failed.";

	/**
	 * Initializes a new WalletDecodingException.
	 */
	public WalletDecodingException(IndyError error)
	{
		super(message+ error.buildMessage(), ErrorCode.WalletDecodingError.value());
	}
}