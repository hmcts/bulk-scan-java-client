package uk.gov.hmcts.bulkscan.exception;

import static uk.gov.hmcts.bulkscan.type.ErrorCode.ERR_ZIP_PROCESSING_FAILED;

public class NonPdfFileFoundException extends EnvelopeRejectionException {

    private static final long serialVersionUID = 9143161748679833084L;

    public NonPdfFileFoundException(String zipFileName, String fileName) {
        super(ERR_ZIP_PROCESSING_FAILED, "Zip '" + zipFileName + "' contains non-pdf file: " + fileName);
    }
}
