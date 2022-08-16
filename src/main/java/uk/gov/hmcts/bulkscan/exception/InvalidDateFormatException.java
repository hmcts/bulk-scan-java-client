package uk.gov.hmcts.bulkscan.exception;

public class InvalidDateFormatException extends RuntimeException {

    private static final long serialVersionUID = -1529317708014458414L;

    public InvalidDateFormatException(String format) {
        this(format, null);
    }

    public InvalidDateFormatException(String format, Throwable cause) {
        super(format, cause);
    }
}
