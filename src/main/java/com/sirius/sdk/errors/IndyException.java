package com.sirius.sdk.errors;


import com.sirius.sdk.errors.indy_exceptions.*;
import org.json.JSONObject;

/**
 * Thrown when an Indy specific error has occurred.
 */
public class IndyException extends Exception {

    private static final long serialVersionUID = 2650355290834266477L;
    private int sdkErrorCode;
    private String sdkMessage;
    private String sdkBacktrace; // Collecting of backtrace can be enabled by:
    //   1) setting environment variable `RUST_BACKTRACE=1`
    //   2) calling `setRuntimeConfig` API function with `collect_backtrace: true`

    public String getInternalMessage() {
        return "";
    }



    /**
     * Initializes a new IndyException with the specified message.
     *
     * @param message      The message for the exception.
     * @param sdkErrorCode The SDK error code to construct the exception from.
     */
    protected IndyException(String message, int sdkErrorCode) {
        super(message);
        IndyError errorDetails = new IndyError();
        this.sdkErrorCode = sdkErrorCode;
        this.sdkMessage = errorDetails.message;
        this.sdkBacktrace = errorDetails.backtrace;
    }

    /**
     * Initializes a new IndyException with the specified message.
     *
     * @param errorDetails The details for the exception.
     * @param sdkErrorCode The SDK error code to construct the exception from.
     */
    protected IndyException(IndyError errorDetails, int sdkErrorCode) {
        super(errorDetails.buildMessage());
        this.sdkErrorCode = sdkErrorCode;
        this.sdkMessage = errorDetails.message;
        this.sdkBacktrace = errorDetails.backtrace;
    }

    /**
     * Gets the SDK error code for the exception.
     *
     * @return The SDK error code used to construct the exception.
     */
    public int getSdkErrorCode() {
        return sdkErrorCode;
    }

    /**
     * Gets the SDK error message for the exception.
     */
    public String getSdkMessage() {
        return sdkMessage;
    }

    /**
     * Gets the SDK error backtrace for the exception.
     *
     * @return The SDK backtrace.
     */
    public String getSdkBacktrace() {
        return sdkBacktrace;
    }

    public static class IndyError {
        String message;
        String backtrace;

        private IndyError(JSONObject errorDetails) {
            try {
                this.message = errorDetails.optString("message");
                this.backtrace = errorDetails.optString("backtrace");
            } catch (Exception ignored) {
                // Nothing to do
            }
        }

        private IndyError() {
            //PointerByReference errorDetailsJson = new PointerByReference();
            //	LibIndy.api.indy_get_current_error(errorDetailsJson);
            //JSONObject errorDetails = new JSONObject(errorDetailsJson.getValue().getString(0));
            try {
                JSONObject errorDetails = new JSONObject();
                this.message = errorDetails.optString("message");
                this.backtrace = errorDetails.optString("backtrace");
            } catch (Exception ignored) {
                // Nothing to do
            }
        }

        public String buildMessage() {
            return " message {" + message + "}" + "backtrace {" + backtrace + "}";
        }
    }

    /**
     * Initializes a new IndyException using the specified SDK error code.
     *
     * @param sdkErrorCode The SDK error code to construct the exception from.
     * @return IndyException correspondent to SDK error code
     */
    public static IndyException fromSdkError(int sdkErrorCode, JSONObject errorObject) {

        ErrorCode errorCode = ErrorCode.valueOf(sdkErrorCode);
        IndyError errorDetails = new IndyError(errorObject);
        switch (errorCode) {
            case CommonInvalidParam1:
            case CommonInvalidParam2:
            case CommonInvalidParam3:
            case CommonInvalidParam4:
            case CommonInvalidParam5:
            case CommonInvalidParam6:
            case CommonInvalidParam7:
            case CommonInvalidParam8:
            case CommonInvalidParam9:
            case CommonInvalidParam10:
            case CommonInvalidParam11:
            case CommonInvalidParam12:
            case CommonInvalidParam13:
            case CommonInvalidParam14:
                return new InvalidParameterException(errorDetails, sdkErrorCode);
            case CommonInvalidState:
                return new InvalidStateException(errorDetails);
            case CommonInvalidStructure:
                return new InvalidStructureException(errorDetails);
            case CommonIOError:
                return new IOException(errorDetails);
            case WalletInvalidHandle:
                return new InvalidWalletException(errorDetails);
            case WalletUnknownTypeError:
                return new UnknownWalletTypeException(errorDetails);
            case WalletTypeAlreadyRegisteredError:
                return new DuplicateWalletTypeException(errorDetails);
            case WalletAlreadyExistsError:
                return new WalletExistsException(errorDetails);
            case WalletNotFoundError:
                return new WalletNotFoundException(errorDetails);
            case WalletInputError:
                return new WalletInputException(errorDetails);
            case WalletDecodingError:
                return new WalletDecodingException(errorDetails);
            case WalletStorageError:
                return new WalletStorageException(errorDetails);
            case WalletEncryptionError:
                return new WalletEncryptionException(errorDetails);
            case WalletItemNotFound:
                return new WalletItemNotFoundException(errorDetails);
            case WalletItemAlreadyExists:
                return new WalletItemAlreadyExistsException(errorDetails);
            case WalletQueryError:
                return new WalletInvalidQueryException(errorDetails);
            case WalletIncompatiblePoolError:
                return new WrongWalletForPoolException(errorDetails);
            case WalletAlreadyOpenedError:
                return new WalletAlreadyOpenedException(errorDetails);
            case WalletAccessFailed:
                return new WalletAccessFailedException(errorDetails);
            case PoolLedgerNotCreatedError:
                return new PoolConfigNotCreatedException(errorDetails);
            case PoolLedgerInvalidPoolHandle:
                return new InvalidPoolException(errorDetails);
            case PoolLedgerTerminated:
                return new PoolLedgerTerminatedException(errorDetails);
            case LedgerNoConsensusError:
                return new ConsensusException(errorDetails);
            case LedgerInvalidTransaction:
                return new LedgerInvalidTransactionException(errorDetails);
            case LedgerSecurityError:
                return new LedgerSecurityException(errorDetails);
            case PoolLedgerConfigAlreadyExistsError:
                return new PoolLedgerConfigExistsException(errorDetails);
            case PoolLedgerTimeout:
                return new TimeoutException(errorDetails);
            case PoolIncompatibleProtocolVersion:
                return new PoolIncompatibleProtocolVersionException(errorDetails);
            case LedgerNotFound:
                return new LedgerNotFoundException(errorDetails);
            case AnoncredsRevocationRegistryFullError:
                return new RevocationRegistryFullException(errorDetails);
            case AnoncredsInvalidUserRevocId:
                return new AnoncredsInvalidUserRevocId(errorDetails);
            case AnoncredsMasterSecretDuplicateNameError:
                return new DuplicateMasterSecretNameException(errorDetails);
            case AnoncredsProofRejected:
                return new ProofRejectedException(errorDetails);
            case AnoncredsCredentialRevoked:
                return new CredentialRevokedException(errorDetails);
            case AnoncredsCredDefAlreadyExistsError:
                return new CredDefAlreadyExistsException(errorDetails);
            case UnknownCryptoTypeError:
                return new UnknownCryptoException(errorDetails);
            case DidAlreadyExistsError:
                return new DidAlreadyExistsException(errorDetails);
            case UnknownPaymentMethod:
                return new UnknownPaymentMethodException(errorDetails);
            case IncompatiblePaymentError:
                return new IncompatiblePaymentException(errorDetails);
            case InsufficientFundsError:
                return new InsufficientFundsException(errorDetails);
            case ExtraFundsError:
                return new ExtraFundsException(errorDetails);
            case PaymentSourceDoesNotExistError:
                return new PaymentSourceDoesNotExistException(errorDetails);
            case PaymentOperationNotSupportedError:
                return new PaymentOperationNotSupportedException(errorDetails);
            case TransactionNotAllowedError:
                return new TransactionNotAllowedException(errorDetails);
            default:
                String message = String.format("An unmapped error with the code '%s' was returned by the SDK.", sdkErrorCode);
                return new IndyException(message, sdkErrorCode);
        }
    }
}


