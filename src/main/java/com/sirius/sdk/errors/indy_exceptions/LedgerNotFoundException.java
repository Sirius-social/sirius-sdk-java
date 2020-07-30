package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempting to open Pool for witch Genesis Transactions are not compatible with set Protocol version.
 */
public class LedgerNotFoundException extends IndyException
{
	private static final long serialVersionUID = 7935181938462170500L;
	private final static String message = "Item not found on ledger exception.";

	/**
	 * Initializes a new PoolIncompatibleProtocolVersionException.
	 */
	public LedgerNotFoundException()
	{
		super(message, ErrorCode.LedgerNotFound.value());
	}
}