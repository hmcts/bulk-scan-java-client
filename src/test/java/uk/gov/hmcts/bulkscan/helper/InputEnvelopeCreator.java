package uk.gov.hmcts.bulkscan.helper;

import uk.gov.hmcts.bulkscan.type.Classification;
import uk.gov.hmcts.bulkscan.type.InputDocumentType;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.type.InputOcrData;
import uk.gov.hmcts.bulkscan.type.InputPayment;
import uk.gov.hmcts.bulkscan.type.InputScannableItem;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;

public final class InputEnvelopeCreator {

    public static final String ZIP_FILE_NAME = "file.zip";

    private InputEnvelopeCreator() {
        // util class
    }

    public static InputEnvelope inputEnvelope(String jurisdiction) {
        return inputEnvelope(jurisdiction, "poBox", Classification.EXCEPTION, emptyList(), emptyList());
    }

    public static InputEnvelope inputEnvelope(String jurisdiction, String poBox) {
        return inputEnvelope(jurisdiction, poBox, Classification.EXCEPTION, emptyList(), emptyList());
    }

    public static InputEnvelope inputEnvelope(
            String jurisdiction,
            String poBox,
            Classification classification,
            List<InputScannableItem> scannableItems
    ) {
        return inputEnvelope(
                jurisdiction,
                poBox,
                classification,
                scannableItems,
                emptyList()
        );
    }

    public static InputEnvelope inputEnvelope(
            String jurisdiction,
            String poBox,
            Classification classification,
            List<InputScannableItem> scannableItems,
            List<InputPayment> payments
    ) {
        return new InputEnvelope(
                poBox,
                jurisdiction,
                null,
                null,
                null,
                ZIP_FILE_NAME,
                null,
                "case_number",
                "previous_service_case_ref",
                classification,
                scannableItems,
                payments,
                emptyList()
        );
    }

    public static InputScannableItem scannableItem(String fileName) {
        return scannableItem(fileName, InputDocumentType.OTHER);
    }

    public static InputScannableItem scannableItem(String fileName, InputDocumentType documentType) {
        return scannableItem(fileName, UUID.randomUUID().toString(), documentType, new InputOcrData());
    }

    public static InputScannableItem scannableItem(String fileName, String dcn) {
        return scannableItem(fileName, dcn, InputDocumentType.OTHER, new InputOcrData());
    }

    public static InputScannableItem scannableItem(InputDocumentType documentType, InputOcrData ocrData) {
        return scannableItem("file.pdf", UUID.randomUUID().toString(), documentType, ocrData);
    }

    public static InputScannableItem scannableItem(
            String fileName,
            String dcn,
            InputDocumentType documentType,
            InputOcrData ocrData
    ) {
        return new InputScannableItem(
                dcn,
                null,
                "ocr_accuracy",
                "manula_intervention",
                "next_action",
                null,
                ocrData,
                fileName,
                "notes",
                documentType,
                null
        );
    }

    public static InputPayment payment(String documentControlNumber) {
        return new InputPayment(documentControlNumber);
    }
}