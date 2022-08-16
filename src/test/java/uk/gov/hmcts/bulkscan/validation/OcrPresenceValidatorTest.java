package uk.gov.hmcts.bulkscan.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.bulkscan.exception.OcrPresenceException;
import uk.gov.hmcts.bulkscan.type.InputDocumentType;
import uk.gov.hmcts.bulkscan.type.InputOcrData;
import uk.gov.hmcts.bulkscan.type.InputScannableItem;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class OcrPresenceValidatorTest {

    private final OcrPresenceValidator validator = new OcrPresenceValidator();

    @Test
    void should_throw_exception_if_multiple_docs_contain_ocr() {
        final List<InputScannableItem> docs = asList(
                doc(InputDocumentType.FORM, new InputOcrData()),
                doc(InputDocumentType.OTHER, new InputOcrData()),
                doc(InputDocumentType.OTHER, null),
                doc(InputDocumentType.CHERISHED, null)
        );

        assertThatThrownBy(
                () ->
                        validator.assertHasProperlySetOcr(
                                docs
                        )
        )
                .isInstanceOf(OcrPresenceException.class)
                .hasMessage(OcrPresenceValidator.MULTIPLE_OCR_MSG);
    }

    @Test
    void should_throw_an_exception_when_form_has_no_ocr() {
        final List<InputScannableItem> docs = asList(
                doc(InputDocumentType.FORM, null),
                doc(InputDocumentType.OTHER, null),
                doc(InputDocumentType.OTHER, null),
                doc(InputDocumentType.CHERISHED, null)
        );

        assertThatThrownBy(
                () ->
                        validator.assertHasProperlySetOcr(
                                docs
                        )
        )
                .isInstanceOf(OcrPresenceException.class)
                .hasMessage(OcrPresenceValidator.MISSING_OCR_MSG);
    }

    @Test
    void should_throw_an_exception_when_a_doc_that_is_not_form_or_sscs1_has_ocr() {
        List<InputScannableItem> docs = asList(
                doc(InputDocumentType.FORM, null),
                doc(InputDocumentType.OTHER, new InputOcrData()),
                doc(InputDocumentType.OTHER, null),
                doc(InputDocumentType.CHERISHED, null)
        );

        EnumSet
                .allOf(InputDocumentType.class)
                .stream()
                .filter(docType -> !OcrPresenceValidator.OCR_DOC_TYPES.contains(docType))
                .forEach(invalidDocType -> {
                    assertThatThrownBy(
                            () ->
                                    validator.assertHasProperlySetOcr(
                                            docs
                                    )
                    )
                            .isInstanceOf(OcrPresenceException.class)
                            .hasMessage(OcrPresenceValidator.MISPLACED_OCR_MSG);
                });
    }

    @Test
    void should_throw_exception_doc_with_ocr_has_no_subtype() {
        List<InputScannableItem> docs = asList(
                doc(InputDocumentType.FORM, null, new InputOcrData()), // missing subtype
                doc(InputDocumentType.OTHER, "some-subtype-1", null),
                doc(InputDocumentType.CHERISHED, "some-subtype-2", null)
        );

        assertThatThrownBy(
                () ->
                        validator.assertHasProperlySetOcr(
                                docs
                        )
        )
                .isInstanceOf(OcrPresenceException.class)
                .hasMessage(OcrPresenceValidator.MISSING_DOC_SUBTYPE_MSG);
    }

    @Test
    void should_return_document_with_ocr_when_doctype_is_sscs1_and_subtype_is_not_set() {
        // given
        InputScannableItem docWithOcr = doc(InputDocumentType.SSCS1, null, new InputOcrData());
        List<InputScannableItem> docs =
                asList(
                        docWithOcr,
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.CHERISHED, null)
                );

        // when
        Optional<InputScannableItem> result = validator.assertHasProperlySetOcr(docs);

        // then
        assertThat(result).get().isEqualTo(docWithOcr);
    }

    @Test
    void should_return_document_with_ocr() {
        // given
        InputScannableItem docWithOcr = doc(InputDocumentType.FORM, new InputOcrData());
        List<InputScannableItem> docs =
                asList(
                        docWithOcr,
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.CHERISHED, null)
                );

        // when
        Optional<InputScannableItem> result = validator.assertHasProperlySetOcr(docs);

        // then
        assertThat(result).get().isEqualTo(docWithOcr);
    }

    @Test
    void should_return_empty_optional_if_there_are_no_docs_with_ocr() {
        // given
        List<InputScannableItem> docs =
                asList(
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.OTHER, null),
                        doc(InputDocumentType.CHERISHED, null)
                );

        // when
        Optional<InputScannableItem> result = validator.assertHasProperlySetOcr(docs);

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
