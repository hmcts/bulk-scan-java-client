package uk.gov.hmcts.bulkscan.exception;

public class BulkScanServerException extends RuntimeException {

    private static final long serialVersionUID = 8401476226425726980L;

    public BulkScanServerException() {
        super();
    }

    public BulkScanServerException(String message) {
        super(message);
    }

    public BulkScanServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public BulkScanServerException(Throwable cause) {
        super(cause);
    }
}
