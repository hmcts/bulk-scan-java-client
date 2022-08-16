package uk.gov.hmcts.bulkscan.exception;

import static uk.gov.hmcts.bulkscan.type.ErrorCode.ERR_METAFILE_INVALID;

public class DisallowedDocumentTypesException extends EnvelopeRejectionException {

    public DisallowedDocumentTypesException(String message) {
        super(ERR_METAFILE_INVALID, message);
    }
}
