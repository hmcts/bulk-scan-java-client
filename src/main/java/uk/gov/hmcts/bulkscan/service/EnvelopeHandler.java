package uk.gov.hmcts.bulkscan.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.bulkscan.type.IServiceOcrValidator;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.type.OcrValidationWarnings;
import uk.gov.hmcts.bulkscan.validation.BulkScanOcrValidator;
import uk.gov.hmcts.bulkscan.validation.EnvelopeValidator;

import java.util.List;
import java.util.Optional;

/**
 * This class is in charge of handling input envelopes.
 * It will do below things:
 * <ol>
 * <li>Validate input envelope</li>
 * <li>Verify it did not fail to upload before</li>
 * <li>Validate its OCR data</li>
 * <li>Create DB envelope entity and save it to DB</li>
 * </ol>
 */
@Component
public class EnvelopeHandler {

    private final EnvelopeValidator envelopeValidator;

    private final BulkScanOcrValidator bulkScanOcrValidator;

    public EnvelopeHandler(
        EnvelopeValidator envelopeValidator,
        BulkScanOcrValidator bulkScanOcrValidator
    ) {
        this.envelopeValidator = envelopeValidator;
        this.bulkScanOcrValidator = bulkScanOcrValidator;
    }

    public Optional<OcrValidationWarnings> handleEnvelope(
        String zipFilename,
        List<String> pdfs,
        InputEnvelope inputEnvelope,
        IServiceOcrValidator ocrValidator
    ) {
        envelopeValidator.assertZipFilenameMatchesWithMetadata(inputEnvelope, zipFilename);
        envelopeValidator.assertEnvelopeContainsOcrDataIfRequired(inputEnvelope);
        envelopeValidator.assertEnvelopeHasPdfs(inputEnvelope, pdfs);
        envelopeValidator.assertDocumentControlNumbersAreUnique(inputEnvelope);
        envelopeValidator.assertEnvelopeContainsDocsOfAllowedTypesOnly(inputEnvelope);

        return bulkScanOcrValidator.assertOcrDataIsValid(
            inputEnvelope,
            ocrValidator
        );
    }
}
