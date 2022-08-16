package uk.gov.hmcts.bulkscan.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.bulkscan.enums.EnvelopeProcessStatus;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnvelopeProcessAttempt {

    @JsonProperty("attemptId")
    public final UUID attemptId;
    @JsonProperty("envelopeId")
    public final String envelopeId;
    @JsonProperty("serviceName")
    public final String serviceName;
    @JsonProperty("timestamp")
    public final String timestamp;
    @JsonProperty("description")
    public final String description;
    @JsonProperty("warnings")
    public final List<String> warnings;
    @JsonProperty("errors")
    public final List<String> errors;
    @JsonProperty("status")
    public final EnvelopeProcessStatus status;

    public EnvelopeProcessAttempt(UUID attemptId,
                                  String envelopeId,
                                  String serviceName,
                                  String timestamp,
                                  String description,
                                  List<String> warnings,
                                  List<String> errors,
                                  EnvelopeProcessStatus status) {
        this.attemptId = attemptId;
        this.envelopeId = envelopeId;
        this.serviceName = serviceName;
        this.timestamp = timestamp;
        this.description = description;
        this.warnings = warnings;
        this.errors = errors;
        this.status = status;
    }

}

