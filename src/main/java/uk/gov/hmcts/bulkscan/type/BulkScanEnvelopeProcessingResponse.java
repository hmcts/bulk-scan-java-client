package uk.gov.hmcts.bulkscan.type;

import uk.gov.hmcts.bulkscan.enums.EnvelopeProcessStatus;

import java.util.List;

public record BulkScanEnvelopeProcessingResponse(
    String envelopeETag,
    String description,
    EnvelopeProcessStatus status,
    List<String> warnings,
    List<String> errors) {
}
