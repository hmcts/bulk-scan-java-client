package uk.gov.hmcts.bulkscan.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.hmcts.bulkscan.exception.OcrDataParseException;
import uk.gov.hmcts.bulkscan.type.InputOcrData;

import java.io.IOException;
import java.util.Base64;

public class OcrDataDeserializer extends StdDeserializer<InputOcrData> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final long serialVersionUID = -879518026770096586L;

    public OcrDataDeserializer() {
        super(InputOcrData.class);
    }

    @Override
    public InputOcrData deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws OcrDataParseException {
        try {
            return parseOcrData(jsonParser.getText());
        } catch (Exception ex) {
            throw new OcrDataParseException(jsonParser, "Failed to parse OCR data", ex);
        }
    }

    private InputOcrData parseOcrData(String base64EncodedOcrData) throws IOException {
        String ocrDataJson = new String(Base64.getDecoder().decode(base64EncodedOcrData));
        return objectMapper.readValue(ocrDataJson, InputOcrData.class);
    }
}
