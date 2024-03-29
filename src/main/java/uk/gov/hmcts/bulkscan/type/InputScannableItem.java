package uk.gov.hmcts.bulkscan.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import uk.gov.hmcts.bulkscan.util.InstantDeserializer;
import uk.gov.hmcts.bulkscan.util.OcrDataDeserializer;

import java.time.Instant;

public class InputScannableItem {

    public final String documentControlNumber;
    public final Instant scanningDate;
    public final String ocrAccuracy;
    public final String manualIntervention;
    public final String nextAction;
    public final Instant nextActionDate;
    public final InputOcrData ocrData;
    public final String fileName;
    public final String notes;
    public final InputDocumentType documentType;
    public final String documentSubtype;

    @JsonCreator
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public InputScannableItem(
        @JsonProperty("document_control_number") String documentControlNumber,
        @JsonDeserialize(using = InstantDeserializer.class)
        @JsonProperty("scanning_date") Instant scanningDate,
        @JsonProperty("ocr_accuracy") String ocrAccuracy,
        @JsonProperty("manual_intervention") String manualIntervention,
        @JsonProperty("next_action") String nextAction,
        @JsonDeserialize(using = InstantDeserializer.class)
        @JsonProperty("next_action_date") Instant nextActionDate,
        @JsonDeserialize(using = OcrDataDeserializer.class)
        @JsonProperty("ocr_data") InputOcrData ocrData,
        @JsonProperty("file_name") String fileName,
        @JsonProperty("notes") String notes,
        @JsonProperty("document_type") InputDocumentType documentType,
        @JsonProperty("document_sub_type") String documentSubtype

    ) {
        this.documentControlNumber = documentControlNumber;
        this.scanningDate = scanningDate;
        this.ocrAccuracy = ocrAccuracy;
        this.manualIntervention = manualIntervention;
        this.nextAction = nextAction;
        this.nextActionDate = nextActionDate;
        this.ocrData = ocrData;
        this.fileName = fileName;
        this.notes = notes;
        this.documentType = documentType;
        this.documentSubtype = documentSubtype;
    }
}
