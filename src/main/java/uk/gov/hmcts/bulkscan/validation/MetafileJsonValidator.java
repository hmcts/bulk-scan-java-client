package uk.gov.hmcts.bulkscan.validation;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.bulkscan.exception.InvalidEnvelopeSchemaException;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;

import java.io.IOException;
import java.io.InputStream;

@Component
public class MetafileJsonValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

    private final JsonSchema jsonSchemaValidator;

    public MetafileJsonValidator() throws IOException, ProcessingException {
        // library only supports up to draft-04 of json schema
        try (InputStream inputStream = getClass().getResourceAsStream("/metafile-schema.json")) {
            jsonSchemaValidator = JsonSchemaFactory
                .byDefault()
                .getJsonSchema(MAPPER.readTree(inputStream));
        }
    }

    /**
     * Validate object against envelope schema.
     * Throws an {@code InvalidEnvelopeSchemaException} in case there are errors.
     *
     * @param metafile to validate against
     * @throws ProcessingException processing error during the validation
     */
    public void validate(byte[] metafile, String zipFileName) throws IOException, ProcessingException {
        ProcessingReport report = jsonSchemaValidator.validate(MAPPER.readTree(metafile), true);

        if (!report.isSuccess()) {
            throw new InvalidEnvelopeSchemaException(report, zipFileName);
        }
    }

    public InputEnvelope parseMetafile(byte[] metafile) throws IOException {
        return MAPPER.readValue(metafile, InputEnvelope.class);
    }
}

