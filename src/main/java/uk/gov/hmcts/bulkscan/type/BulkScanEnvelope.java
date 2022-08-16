package uk.gov.hmcts.bulkscan.type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkScanEnvelope {

    @JsonProperty("etag")
    private String etag;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("url")
    private String url;

    @JsonProperty("createdAt")
    //"2022-05-09T11:47:28Z"
    private String createdAt;

    @JsonProperty("contentLength")
    private long contentLength;

    @JsonProperty("contentType")
    private String contentType;
}
