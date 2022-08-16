package uk.gov.hmcts.bulkscan.exception;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Represents a situation where OCR data format is invalid.
 *
 * <p>
 * Needs to extend JsonMappingException, so that we can catch it - otherwise
 * Jackson would wrap it in its own exception when parsing metadata file.
 * </p>
 */
public class OcrDataParseException extends JsonMappingException {

    private static final long serialVersionUID = 1409451618781693868L;

    public OcrDataParseException(JsonParser jsonParser, String message, Throwable cause) {
        super(jsonParser, message, cause);
    }
}
