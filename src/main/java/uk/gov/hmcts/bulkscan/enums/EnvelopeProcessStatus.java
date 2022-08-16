package uk.gov.hmcts.bulkscan.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EnvelopeProcessStatus {
    @JsonProperty("SUCCESS")
    SUCCESS,
    @JsonProperty("SUCCESS_WITH_WARNINGS")
    SUCCESS_WITH_WARNINGS,
    @JsonProperty("FATAL")
    FATAL,
    @JsonProperty("ERRORS")
    ERRORS
}
