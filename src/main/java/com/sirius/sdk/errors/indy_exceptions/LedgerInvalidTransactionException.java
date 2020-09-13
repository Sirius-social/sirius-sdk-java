package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempt to parse invalid transaction response.
 */
public class LedgerInvalidTransactionException extends IndyException
{
	private static final long serialVersionUID = -6503578332467229584L;
	private final static String message = "No consensus was reached during the ledger operation.";

	/**
	 * Initializes a new LedgerInvalidTransactionException.
	 */
	public LedgerInvalidTransactionException(IndyError error)
	{
		super(message  +error.buildMessage(), ErrorCode.LedgerInvalidTransaction.value());
	}
}