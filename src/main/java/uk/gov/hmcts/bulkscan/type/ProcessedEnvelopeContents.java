package uk.gov.hmcts.bulkscan.type;

import java.util.Optional;

public record ProcessedEnvelopeContents(BulkScanEnvelope envelope,
                                        ZipFileContentDetail zipDetail,
                                        InputEnvelope inputEnvelope,
                                        Optional<OcrValidationWarnings> warnings) {


}
