package com.sirius.sdk.errors.indy_exceptions;


import com.sirius.sdk.errors.ErrorCode;
import com.sirius.sdk.errors.IndyException;

/**
 * Exception thrown when add record operation is used with record name that already exists.
 */
public class WalletItemAlreadyExistsException extends IndyException
{
	private static final long serialVersionUID = 667964860056778208L;
	private final static String message = "Item already exists.";

	/**
	 * Initializes a new WalletItemNotFoundException.
	 */
	public WalletItemAlreadyExistsException(IndyError error) {
		super(message + error.buildMessage(), ErrorCode.WalletItemAlreadyExists.value());
	}
}
