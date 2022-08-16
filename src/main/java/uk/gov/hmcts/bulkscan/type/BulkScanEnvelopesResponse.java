package uk.gov.hmcts.bulkscan.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkScanEnvelopesResponse {

    @JsonProperty("count")
    private int count;

    @JsonProperty("data")
    private List<BulkScanEnvelope> data;
}
