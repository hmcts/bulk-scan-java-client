package uk.gov.hmcts.bulkscan.type;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.bulkscan.exception.OcrPresenceException;
import uk.gov.hmcts.bulkscan.helper.InputEnvelopeCreator;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class InputEnvelopeTest {

    List<InputOcrDataField> ocrDataFields = Arrays.asList(
            new InputOcrDataField(new TextNode("text_field"), new TextNode("some text")),
            new InputOcrDataField(new TextNode("number_field"), new IntNode(123)),
            new InputOcrDataField(new TextNode("boolean_field"), BooleanNode.TRUE),
            new InputOcrDataField(new TextNode("null_field"), NullNode.instance)
    );

    @Test
    void should_throw_exception_if_multiple_docs_contain_ocr() {

        var ocrData = new InputOcrData();
        ocrData.setFields(ocrDataFields);

        var inputEnvelope = InputEnvelopeCreator.inputEnvelope(
                "test",
                "test",
                Classification.NEW_APPLICATION,
                asList(
                        InputEnvelopeCreator.scannableItem(
                                InputDocumentType.FORM,
                                ocrData
                        ),
                        InputEnvelopeCreator.scannableItem(
                                InputDocumentType.OTHER,
                                ocrData
                        ),
                        InputEnvelopeCreator.scannableItem(
                                "form",
                                InputDocumentType.OTHER
                        ),
                        InputEnvelopeCreator.scannableItem(
                                "form",
                                InputDocumentType.CHERISHED
                        )
                )
        );

        assertThatThrownBy(
                inputEnvelope::findDocWithOcr
        )
                .isInstanceOf(OcrPresenceException.class)
                .hasMessage(InputEnvelope.MULTIPLE_OCR_MSG);
    }

    @Test
    void should_throw_an_exception_when_form_has_no_ocr() {
        var inputEnvelope = InputEnvelopeCreator.inputEnvelope(
                "test",
                "test",
                Classification.NEW_APPLICATION,
                asList(
                    doc(InputDocumentType.FORM, null),
                    doc(InputDocumentType.OTHER, null),
                    doc(InputDocumentType.OTHER, null),
                    doc(InputDocumentType.CHERISHED, null)
                )
        );

        assertThatThrownBy(
                inputEnvelope::findDocWithOcr
        )
                .isInstanceOf(OcrPresenceException.class)
                .hasMessage(InputEnvelope.MISSING_OCR_MSG);
    }

    @Test
    void should_throw_an_exception_when_a_doc_that_is_not_form_or_sscs1_has_ocr() {
        var inputEnvelope = InputEnvelopeCreator.inputEnvelope(
                "test",
                "test",
                Classification.NEW_APPLICATION,
                asList(
                    doc(InputDocumentType.FORM, null),
                    doc(InputDocumentType.OTHER, new InputOcrData()),
                    doc(InputDocumentType.OTHER, null),
                    doc(InputDocumentType.CHERISHED, null)
                )
        );

        EnumSet
                .allOf(InputDocumentType.class)
                .stream()
                .filter(docType -> !InputEnvelope.OCR_DOC_TYPES.contains(docType))
                .forEach(invalidDocType -> {
                    assertThatThrownBy(
                            inputEnvelope::findDocWithOcr
                    )
                            .isInstanceOf(OcrPresenceException.class)
                            .hasMessage(InputEnvelope.MISPLACED_OCR_MSG);
                });
    }

    @Test
    void should_throw_exception_doc_with_ocr_has_no_subtype() {
        var inputEnvelope = InputEnvelopeCreator.inputEnvelope(
                "test",
                "test",
                Classification.NEW_APPLICATION,
                asList(
                    doc(InputDocumentType.FORM, null, new InputOcrData()), // missing subtype
                    doc(InputDocumentType.OTHER, "some-subtype-1", null),
                    doc(InputDocumentType.CHERISHED, "some-subtype-2", null)
                )
        );

        assertThatThrownBy(
                inputEnvelope::findDocWithOcr
        )
                .isInstanceOf(OcrPresenceException.class)
                .hasMessage(InputEnvelope.MISSING_DOC_SUBTYPE_MSG);
    }

    @Test
    void should_return_document_with_ocr_when_doctype_is_sscs1_and_subtype_is_not_set() {
        // given
        InputScannableItem docWithOcr = doc(InputDocumentType.SSCS1, null, new InputOcrData());
        var inputEnvelope = InputEnvelopeCreator.inputEnvelope(
                "test",
                "test",
                Classification.NEW_APPLICATION,
                asList(
                        docWithOcr,
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.CHERISHED, null)
                )
        );

        // when
        Optional<InputScannableItem> result = inputEnvelope.findDocWithOcr();

        // then
        assertThat(result).get().isEqualTo(docWithOcr);
    }

    @Test
    void should_return_document_with_ocr() {
        // given
        InputScannableItem docWithOcr = doc(InputDocumentType.FORM, new InputOcrData());
        var inputEnvelope = InputEnvelopeCreator.inputEnvelope(
                "test",
                "test",
                Classification.NEW_APPLICATION,
                asList(
                        docWithOcr,
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.CHERISHED, null)
                )
        );

        // when
        Optional<InputScannableItem> result = inputEnvelope.findDocWithOcr();

        // then
        assertThat(result).get().isEqualTo(docWithOcr);
    }

    @Test
    void should_return_empty_optional_if_there_are_no_docs_with_ocr() {
        // given
        var inputEnvelope = InputEnvelopeCreator.inputEnvelope(
                "test",
                "test",
                Classification.NEW_APPLICATION,
                asList(
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.CHERISHED, null)
                )
        );

        // when
        Optional<InputScannableItem> result = inputEnvelope.findDocWithOcr();

        // then
        assertThat(result).isEmpty();
    }

    private InputScannableItem doc(InputDocumentType type, InputOcrData ocr) {
        return doc(type, "some-doc-subtype", ocr);
    }

    private InputScannableItem doc(InputDocumentType type, String subtype, InputOcrData ocr) {
        return new InputScannableItem(
                null,
                null,
                null,
                null,
                null,
                null,
                ocr,
                null,
                null,
                type,
                subtype
        );
    }

    @Test
    public void shouldNotHaveProperlySetOcr() {

    }
}
