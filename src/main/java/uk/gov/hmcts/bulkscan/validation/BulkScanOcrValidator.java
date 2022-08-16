package uk.gov.hmcts.bulkscan.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.bulkscan.exception.OcrValidationException;
import uk.gov.hmcts.bulkscan.type.FormData;
import uk.gov.hmcts.bulkscan.type.IServiceOcrValidator;
import uk.gov.hmcts.bulkscan.type.InputDocumentType;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.type.InputScannableItem;
import uk.gov.hmcts.bulkscan.type.OcrDataField;
import uk.gov.hmcts.bulkscan.type.OcrValidationStatus;
import uk.gov.hmcts.bulkscan.type.OcrValidationWarnings;

import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.bulkscan.type.Classification.EXCEPTION;

@Component
public class BulkScanOcrValidator {

    private static final Logger log = LoggerFactory.getLogger(BulkScanOcrValidator.class);

    private final OcrPresenceValidator presenceValidator;

    public BulkScanOcrValidator(
        OcrPresenceValidator presenceValidator
    ) {
        this.presenceValidator = presenceValidator;
    }

    /**
     * If required, validates the OCR data of the given envelope.
     *
     * @return Warnings for valid OCR data, to be displayed to the caseworker. Empty if
     *         no validation took place.
     * @throws OcrValidationException if the OCR data is invalid
     */
    public Optional<OcrValidationWarnings> assertOcrDataIsValid(
        InputEnvelope envelope,
        IServiceOcrValidator validator
    ) {
        if (envelope.classification == EXCEPTION) {
            return Optional.empty();
        }

        return findDocWithOcr(envelope)
            .map(docWithOcr -> {
                var res = validator.validateEnvelope(getFormType(docWithOcr), toFormData(docWithOcr));
                if (res.status().equals(OcrValidationStatus.ERRORS)) {
                    var errorMessage = "OCR validation service returned OCR-specific errors. "
                        + "Document control number: " + docWithOcr.documentControlNumber + ". "
                        + "Envelope: " + envelope.zipFileName + ".";
                    log.info("Found {} errors when processing envelope {}. {}",
                        res.errors().size(),
                        envelope.zipFileName,
                        String.join(", ", res.errors()));
                    throw new OcrValidationException(
                        errorMessage,
                        "OCR fields validation failed. Validation errors: " + res.errors()
                    );
                }

                if (res.status().equals(OcrValidationStatus.SUCCESS)) {
                    log.info(
                        "OCR completed successfully for DCN: {}, doc type: {}, doc subtype: {}, envelope: {}",
                        docWithOcr.documentControlNumber,
                        docWithOcr.documentType,
                        docWithOcr.documentSubtype,
                        envelope.zipFileName
                    );
                    return null;
                }

                if (res.status().equals(OcrValidationStatus.WARNINGS)) {
                    log.info(
                        "Validation ended with warnings. File name: {}",
                        envelope.zipFileName
                    );
                    return new OcrValidationWarnings(
                        docWithOcr.documentControlNumber,
                        res.warnings() != null ? res.warnings() : emptyList()
                    );
                }
                var errorMessage = String.format("Error validating DCN: %s, doc type: %s, doc subtype: %s, envelope: %s.",
                    docWithOcr.documentControlNumber,
                    docWithOcr.documentType,
                    docWithOcr.documentSubtype,
                    envelope.zipFileName);
                log.error(errorMessage);
                throw new OcrValidationException(errorMessage);
            });
    }

    private Optional<InputScannableItem> findDocWithOcr(InputEnvelope envelope) {
        return presenceValidator.assertHasProperlySetOcr(envelope.scannableItems);
    }

    private FormData toFormData(InputScannableItem doc) {
        return new FormData(
            doc
                .ocrData
                .getFields()
                .stream()
                .map(it -> new OcrDataField(it.name.asText(), it.value.textValue()))
                .collect(toList())
        );
    }

    private String getFormType(InputScannableItem item) {
        return item.documentType == InputDocumentType.SSCS1 ? "SSCS1" : item.documentSubtype;
    }
}
