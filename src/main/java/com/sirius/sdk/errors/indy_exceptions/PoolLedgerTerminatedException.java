package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when a pool ledger has been terminated.
 */
public class PoolLedgerTerminatedException extends IndyException
{
	private static final long serialVersionUID = 768482152424714514L;
	private final static String message = "The pool ledger was terminated.";

	/**
	 * Initializes a new PoolLedgerTerminatedException.
	 */
	public PoolLedgerTerminatedException(IndyError error)
	{
		super(message+error.buildMessage(), ErrorCode.PoolLedgerTerminated.value());
	}
}