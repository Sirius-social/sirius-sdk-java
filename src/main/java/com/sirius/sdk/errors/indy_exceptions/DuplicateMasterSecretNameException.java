package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when attempting to create a master secret name that already exists.
 */
public class DuplicateMasterSecretNameException extends IndyException
{
	private static final long serialVersionUID = 7180454759216991453L;
	private final static String message = "Another master-secret with the specified name already exists.";

	/**
	 * Initializes a new DuplicateMasterSecretNameException.
	 */
	public DuplicateMasterSecretNameException(IndyError error)
	{
		super(message + error.buildMessage(), ErrorCode.AnoncredsMasterSecretDuplicateNameError.value());
	}
}