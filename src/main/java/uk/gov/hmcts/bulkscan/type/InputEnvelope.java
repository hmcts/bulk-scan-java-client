package uk.gov.hmcts.bulkscan.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.bulkscan.exception.OcrPresenceException;
import uk.gov.hmcts.bulkscan.util.InstantDeserializer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.bulkscan.type.InputDocumentType.FORM;
import static uk.gov.hmcts.bulkscan.type.InputDocumentType.SSCS1;

public class InputEnvelope {

    public final String caseNumber;
    public final String previousServiceCaseReference;
    public final String poBox;
    public final String jurisdiction;
    public final Instant deliveryDate;
    public final Instant openingDate;
    public final Instant zipFileCreateddate;
    public final String zipFileName;
    public final String rescanFor;
    public final Classification classification;
    public final List<InputScannableItem> scannableItems;
    public final List<InputPayment> payments;
    public final List<InputNonScannableItem> nonScannableItems;

    static final String MULTIPLE_OCR_MSG = "Multiple docs with OCR";
    static final String MISSING_OCR_MSG = "Empty OCR on 'form' document";
    static final String MISPLACED_OCR_MSG = "OCR on document of invalid type";
    static final String MISSING_DOC_SUBTYPE_MSG = "Missing subtype on document with OCR";

    static final int MULTIPLE_DOC_CHECK = 1;

    // The only document types that can (and must) have OCR data.
    // Note: remove 'SSCS1' once sscs migrates to the new format.
    protected static final List<InputDocumentType> OCR_DOC_TYPES = List.of(FORM, SSCS1);

    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public InputEnvelope(
            @JsonProperty("po_box") String poBox,
            @JsonProperty("jurisdiction") String jurisdiction,
            @JsonDeserialize(using = InstantDeserializer.class)
            @JsonProperty("delivery_date") Instant deliveryDate,
            @JsonDeserialize(using = InstantDeserializer.class)
            @JsonProperty("opening_date") Instant openingDate,
            @JsonDeserialize(using = InstantDeserializer.class)
            @JsonProperty("zip_file_createddate") Instant zipFileCreateddate,
            @JsonProperty("zip_file_name") String zipFileName,
            @JsonProperty("rescan_for") String rescanFor,
            @JsonProperty("case_number") String caseNumber,
            @JsonProperty("previous_service_case_reference") String previousServiceCaseReference,
            @JsonProperty("envelope_classification") Classification classification,
            @JsonProperty("scannable_items") List<InputScannableItem> scannableItems,
            @JsonProperty("payments") List<InputPayment> payments,
            @JsonProperty("non_scannable_items") List<InputNonScannableItem> nonScannableItems) {
        this.poBox = poBox;
        this.jurisdiction = jurisdiction;
        this.deliveryDate = deliveryDate;
        this.openingDate = openingDate;
        this.zipFileCreateddate = zipFileCreateddate;
        this.zipFileName = zipFileName;
        this.rescanFor = rescanFor;
        // scanning can add spaces
        this.caseNumber = StringUtils.trim(caseNumber);
        this.previousServiceCaseReference = previousServiceCaseReference;
        this.classification = classification;
        this.scannableItems = scannableItems == null ? emptyList() : scannableItems;
        this.payments = payments == null ? emptyList() : payments;
        this.nonScannableItems = nonScannableItems == null ? emptyList() : nonScannableItems;
    }

    public String getFormType() {
        return this
                .scannableItems
                .stream()
                .filter(si -> InputDocumentType.FORM.equals(si.documentType))
                .findFirst()
                .map(si -> si.documentSubtype)
                .orElse(null);
    }

    public Optional<InputScannableItem> findDocWithOcr() {
        return assertHasProperlySetOcr(this.scannableItems);
    }

    public List<OcrDataField> retrieveOcrDataFields() {
        return findScannableItemsWithOcrData()
                .map(item -> convertFromInputOcrData(item.ocrData))
                .findFirst()
                .orElse(null);
    }

    private Stream<InputScannableItem> findScannableItemsWithOcrData() {
        return this
                .scannableItems
                .stream()
                .filter(si -> si.ocrData != null);
    }

    private List<OcrDataField> convertFromInputOcrData(InputOcrData inputOcrData) {
        return inputOcrData
                .getFields()
                .stream()
                .map(this::convertFromInputOcrDataField)
                .collect(toList());
    }

    private OcrDataField convertFromInputOcrDataField(InputOcrDataField inputField) {
        String value = inputField.value != null
                ? inputField.value.asText("")
                : "";

        return new OcrDataField(inputField.name.textValue(), value);
    }

    private Optional<InputScannableItem> assertHasProperlySetOcr(List<InputScannableItem> docs) {

        if (docs.stream().filter(doc -> doc.ocrData != null).count() > MULTIPLE_DOC_CHECK) {
            throw new OcrPresenceException(MULTIPLE_OCR_MSG);
        }
        if (docs.stream().anyMatch(doc -> !OCR_DOC_TYPES.contains(doc.documentType) && doc.ocrData != null)) {
            throw new OcrPresenceException(MISPLACED_OCR_MSG);
        }
        if (docs.stream().anyMatch(doc -> OCR_DOC_TYPES.contains(doc.documentType) && doc.ocrData == null)) {
            throw new OcrPresenceException(MISSING_OCR_MSG);
        }
        // TODO: For SSCS1 we don't receive document subtype as it follows a different contract
        if (docs.stream().anyMatch(
                doc -> doc.documentType != SSCS1 && doc.documentSubtype == null && doc.ocrData != null
        )) {
            throw new OcrPresenceException(MISSING_DOC_SUBTYPE_MSG);
        }

        return docs
                .stream()
                .filter(it -> it.ocrData != null)
                .findFirst();
    }
}

