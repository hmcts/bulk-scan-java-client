package uk.gov.hmcts.bulkscan.type;

public interface IServiceOcrValidator {

    OcrValidationResult validateEnvelope(String formType, FormData docWithOcr);
}
