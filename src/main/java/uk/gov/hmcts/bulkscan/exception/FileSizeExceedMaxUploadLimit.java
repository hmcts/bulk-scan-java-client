package uk.gov.hmcts.bulkscan.exception;

public class FileSizeExceedMaxUploadLimit extends RuntimeException {

    private static final long serialVersionUID = -1322011920524371630L;

    public FileSizeExceedMaxUploadLimit(String message) {
        super(message);
    }
}
