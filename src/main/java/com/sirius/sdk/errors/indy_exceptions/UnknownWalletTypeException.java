package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when opening a wallet while specifying a wallet type that has not been registered.
 */
public class UnknownWalletTypeException extends IndyException
{
	private static final long serialVersionUID = -6275711661964891560L;
	private final static String message = "The wallet type specified has not been registered.";

	/**
	 * Initializes a new UnknownWalletTypeException.
	 */
	public UnknownWalletTypeException(IndyError error)
	{
		super(message + error.buildMessage(), ErrorCode.WalletUnknownTypeError.value());
	}
}