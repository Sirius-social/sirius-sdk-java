package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempting to use a poll that has already been closed.
 */
public class InvalidPoolException extends IndyException
{
	private static final long serialVersionUID = 7124250084655044699L;
	private final static String message = "The pool is closed or invalid and cannot be used.";

	/**
	 * Initializes a new PoolClosedException.
	 */
	public InvalidPoolException(IndyError error)
	{
		super(message  +error.buildMessage(), ErrorCode.PoolLedgerInvalidPoolHandle.value());
	}
}