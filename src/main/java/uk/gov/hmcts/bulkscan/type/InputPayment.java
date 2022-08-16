package uk.gov.hmcts.bulkscan.type;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InputPayment {

    public final String documentControlNumber;

    public InputPayment(
        @JsonProperty("document_control_number") String documentControlNumber
    ) {
        this.documentControlNumber = documentControlNumber;
    }
}
