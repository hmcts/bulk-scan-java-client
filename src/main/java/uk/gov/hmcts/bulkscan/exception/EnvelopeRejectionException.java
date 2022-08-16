package uk.gov.hmcts.bulkscan.exception;

import uk.gov.hmcts.bulkscan.type.ErrorCode;

public abstract class EnvelopeRejectionException extends RuntimeException {

    private static final long serialVersionUID = 4334244041339557973L;
    // might contain sensitive data
    private final String errorDescription;
    private final ErrorCode errorCode;

    protected EnvelopeRejectionException(ErrorCode errorCode, String message) {
        super(message);
        //use message as error description
        this.errorDescription = message;
        this.errorCode = errorCode;
    }

    protected EnvelopeRejectionException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        //use message as error description
        this.errorDescription = message;
        this.errorCode = errorCode;
    }

    protected EnvelopeRejectionException(ErrorCode errorCode, String message, String errorDescription) {
        super(message);
        this.errorDescription = errorDescription;
        this.errorCode = errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

