package uk.gov.hmcts.bulkscan.type;

import java.util.List;

public record OcrValidationResult(OcrValidationStatus status, List<String> warnings, List<String> errors) {
}

