package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempting to use a wallet with a pool other than the pool the wallet was created for.
 */
public class WrongWalletForPoolException extends IndyException
{
	private static final long serialVersionUID = -8931044806844925321L;
	private final static String message = "The wallet specified is not compatible with the open pool.";

	/**
	 * Initializes a new WrongWalletForPoolException.
	 */
	public WrongWalletForPoolException(IndyError error)
	{
		super(message  + error.buildMessage(), ErrorCode.WalletIncompatiblePoolError.value());
	}
}
