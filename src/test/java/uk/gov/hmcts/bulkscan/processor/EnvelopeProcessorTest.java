package uk.gov.hmcts.bulkscan.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.bulkscan.processor.EnvelopeProcessor;
import uk.gov.hmcts.bulkscan.validation.MetafileJsonValidator;


@ExtendWith(MockitoExtension.class)
class EnvelopeProcessorTest {

    @Mock
    private MetafileJsonValidator schemaValidator;

    private EnvelopeProcessor envelopeProcessor;

    @BeforeEach
    void setUp() {
        envelopeProcessor = new EnvelopeProcessor(schemaValidator);
    }

    @Test
    void parseEnvelope_should_error_when_no_metadata() {

    }

    @Test
    void parseEnvelope_should_error_when_JsonParseException() {

    }

    @Test
    void parseEnvelope_should_error_when_OcrDataParseException() {

    }
}