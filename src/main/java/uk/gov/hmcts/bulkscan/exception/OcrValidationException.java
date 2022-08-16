package uk.gov.hmcts.bulkscan.exception;

import static uk.gov.hmcts.bulkscan.type.ErrorCode.ERR_METAFILE_INVALID;

public class OcrValidationException extends EnvelopeRejectionException {

    public OcrValidationException(String message) {
        super(ERR_METAFILE_INVALID, message);
    }

    public OcrValidationException(String message, String detailMessage) {
        super(ERR_METAFILE_INVALID, message, detailMessage);
    }
}
