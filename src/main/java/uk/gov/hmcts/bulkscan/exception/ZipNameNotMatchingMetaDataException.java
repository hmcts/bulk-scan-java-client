package uk.gov.hmcts.bulkscan.exception;

import static uk.gov.hmcts.bulkscan.type.ErrorCode.ERR_METAFILE_INVALID;

public class ZipNameNotMatchingMetaDataException extends EnvelopeRejectionException {

    public ZipNameNotMatchingMetaDataException(String message) {
        super(ERR_METAFILE_INVALID, message);
    }
}
