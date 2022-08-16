package uk.gov.hmcts.bulkscan.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.bulkscan.type.BulkScanEnvelopesResponse;
import uk.gov.hmcts.bulkscan.type.EnvelopeProcessAttempt;

import java.util.UUID;

@FeignClient(
    name = "bulk-scan",
    url = "${bulk-scan.api.url}"
)
public interface BulkScanClientApi {

    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @GetMapping(value = "/envelopes/{serviceName}")
    ResponseEntity<BulkScanEnvelopesResponse> getPendingEnvelopes(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
        @PathVariable String serviceName
    );

    @PutMapping("/envelopes/{serviceName}/{envelopeETag}/process-attempt/{attemptId}")
    ResponseEntity<EnvelopeProcessAttempt> recordEnvelopeProcessingAttempt(
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
        @PathVariable String serviceName,
        @PathVariable String envelopeETag,
        @PathVariable UUID attemptId,
        @RequestBody EnvelopeProcessAttempt processAttempt
    );
}
