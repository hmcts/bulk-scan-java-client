package uk.gov.hmcts.bulkscan.processor;

import com.fasterxml.jackson.core.JsonParseException;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.bulkscan.exception.InvalidEnvelopeSchemaException;
import uk.gov.hmcts.bulkscan.exception.MetadataNotFoundException;
import uk.gov.hmcts.bulkscan.exception.OcrDataParseException;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.validation.MetafileJsonValidator;

import java.io.IOException;
import java.util.Objects;

@Component
public class EnvelopeProcessor {

    private final MetafileJsonValidator schemaValidator;

    public EnvelopeProcessor(
        MetafileJsonValidator schemaValidator
    ) {
        this.schemaValidator = schemaValidator;
    }

    public InputEnvelope parseEnvelope(
        byte[] metadataStream,
        String zipFileName
    ) throws IOException, ProcessingException {
        if (Objects.isNull(metadataStream)) {
            throw new MetadataNotFoundException("No metadata file found in the zip file");
        }

        try {
            schemaValidator.validate(metadataStream, zipFileName);

            return schemaValidator.parseMetafile(metadataStream);
        } catch (JsonParseException | OcrDataParseException exception) {
            // invalid json files should also be reported to provider
            throw new InvalidEnvelopeSchemaException("Error occurred while parsing metafile", exception);
        }
    }
}

