package com.sirius.sdk.errors.indy_exceptions;

import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when a proof has been rejected.
 */
public class ProofRejectedException extends IndyException
{
	private static final long serialVersionUID = -5100028213117687183L;
	private final static String message = "The proof has been rejected.";

	/**
	 * Initializes a new ProofRejectionException.
	 */
	public ProofRejectedException(IndyError error)
	{
		super(message + error.buildMessage(), ErrorCode.AnoncredsProofRejected.value());
	}
}