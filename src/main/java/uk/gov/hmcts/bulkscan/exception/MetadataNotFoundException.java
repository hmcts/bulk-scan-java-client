package uk.gov.hmcts.bulkscan.exception;

import static uk.gov.hmcts.bulkscan.type.ErrorCode.ERR_ZIP_PROCESSING_FAILED;

public class MetadataNotFoundException extends EnvelopeRejectionException {

    public MetadataNotFoundException(String message) {
        super(ERR_ZIP_PROCESSING_FAILED, message);
    }
}
