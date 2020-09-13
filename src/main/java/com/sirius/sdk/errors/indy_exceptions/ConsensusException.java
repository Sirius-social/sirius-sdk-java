package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when consensus was not reached during a ledger operation.
 */
public class ConsensusException extends IndyException
{
	private static final long serialVersionUID = -6503578332467229584L;
	private final static String message = "No consensus was reached during the ledger operation.";

	/**
	 * Initializes a new ConsensusException.
	 */
	public ConsensusException(IndyError error)
	{
		super(message + error.buildMessage(), ErrorCode.LedgerNoConsensusError.value());
	}
}