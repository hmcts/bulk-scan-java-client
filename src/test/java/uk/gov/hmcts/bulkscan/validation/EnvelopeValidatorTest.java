package uk.gov.hmcts.bulkscan.validation;

import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.bulkscan.exception.DisallowedDocumentTypesException;
import uk.gov.hmcts.bulkscan.exception.DuplicateDocumentControlNumbersInEnvelopeException;
import uk.gov.hmcts.bulkscan.exception.FileNameIrregularitiesException;
import uk.gov.hmcts.bulkscan.exception.OcrDataNotFoundException;
import uk.gov.hmcts.bulkscan.exception.ZipNameNotMatchingMetaDataException;
import uk.gov.hmcts.bulkscan.helper.InputEnvelopeCreator;
import uk.gov.hmcts.bulkscan.type.Classification;
import uk.gov.hmcts.bulkscan.type.InputDocumentType;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.type.InputOcrData;
import uk.gov.hmcts.bulkscan.type.InputOcrDataField;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.bulkscan.helper.InputEnvelopeCreator.ZIP_FILE_NAME;
import static uk.gov.hmcts.bulkscan.helper.InputEnvelopeCreator.scannableItem;
import static uk.gov.hmcts.bulkscan.type.Classification.SUPPLEMENTARY_EVIDENCE;

@SuppressWarnings("checkstyle:LineLength")
class EnvelopeValidatorTest {

    private static final String VALIDATION_URL = "https://example.com/validate-ocr";
    private static final String PO_BOX_1 = "sample PO box 1";
    private static final String PO_BOX_2 = "sample PO box 2";
    private static final String JURISDICTION = "jurisdiction";
    private static final String CONTAINER = "container";

    private EnvelopeValidator envelopeValidator;

    @BeforeEach
    void setUp() {
        envelopeValidator = new EnvelopeValidator();
    }

    @Test
    void assertEnvelopeContainsDocsOfAllowedTypesOnly_should_pass() {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "BULKSCAN",
                "POBOX",
                Classification.EXCEPTION,
                singletonList(scannableItem("file1", InputDocumentType.OTHER))
        );

        // when
        // then
        assertDoesNotThrow(() -> envelopeValidator.assertEnvelopeContainsDocsOfAllowedTypesOnly(envelope));
    }

    @Test
    void assertEnvelopeContainsDocsOfAllowedTypesOnly_should_pass_for_supplementary_evidence() {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "BULKSCAN",
                "POBOX",
                SUPPLEMENTARY_EVIDENCE,
                singletonList(scannableItem("file1", InputDocumentType.OTHER))
        );

        // when
        // then
        assertDoesNotThrow(() -> envelopeValidator.assertEnvelopeContainsDocsOfAllowedTypesOnly(envelope));
    }

    @ParameterizedTest
    @EnumSource(
            value = InputDocumentType.class,
            names = {"FORM", "SSCS1"}
    )
    void assertEnvelopeContainsDocsOfAllowedTypesOnly_should_throw_for_disallowed_document_type(InputDocumentType documentType) {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "BULKSCAN",
                "POBOX",
                SUPPLEMENTARY_EVIDENCE,
                singletonList(scannableItem("file1", documentType))
        );

        // when
        // then
        assertThrows(
                DisallowedDocumentTypesException.class,
                () -> envelopeValidator.assertEnvelopeContainsDocsOfAllowedTypesOnly(envelope)
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = Classification.class,
            names = {"EXCEPTION", "SUPPLEMENTARY_EVIDENCE"}
    )
    void assertEnvelopeContainsOcrDataIfRequired_should_pass_for_allowed_classification(Classification classification) {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "BULKSCAN",
                "POBOX",
                classification,
                singletonList(scannableItem("file1", InputDocumentType.OTHER))
        );

        // when
        // then
        assertDoesNotThrow(() -> envelopeValidator.assertEnvelopeContainsOcrDataIfRequired(envelope));
    }

    @ParameterizedTest
    @EnumSource(
            value = Classification.class,
            names = {"NEW_APPLICATION", "SUPPLEMENTARY_EVIDENCE_WITH_OCR"}
    )
    void assertEnvelopeContainsOcrDataIfRequired_should_pass_for_allowed_with_default_document_type(Classification classification) {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "BULKSCAN",
                "POBOX",
                classification,
                singletonList(scannableItem(InputDocumentType.FORM, getOcrData()))
        );

        // when
        // then
        assertDoesNotThrow(() -> envelopeValidator.assertEnvelopeContainsOcrDataIfRequired(envelope));
    }

    @ParameterizedTest
    @EnumSource(
            value = Classification.class,
            names = {"NEW_APPLICATION", "SUPPLEMENTARY_EVIDENCE_WITH_OCR"}
    )
    void assertEnvelopeContainsOcrDataIfRequired_should_throw_if_no_documents_should_have_ocr_data(Classification classification) {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "BULKSCAN",
                "POBOX",
                classification,
                singletonList(scannableItem(InputDocumentType.CHERISHED, getOcrData()))
        );

        // when
        // then
        assertThatThrownBy(
                () -> envelopeValidator.assertEnvelopeContainsOcrDataIfRequired(envelope)
        )
                .isInstanceOf(OcrDataNotFoundException.class)
                .hasMessage("No documents of type Form found");
    }

    @ParameterizedTest
    @EnumSource(
            value = Classification.class,
            names = {"NEW_APPLICATION", "SUPPLEMENTARY_EVIDENCE_WITH_OCR"}
    )
    void assertEnvelopeContainsOcrDataIfRequired_should_throw_if_form_document_has_no_ocr_data(Classification classification) {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "BULKSCAN",
                "POBOX",
                classification,
                singletonList(scannableItem(InputDocumentType.FORM, null))
        );

        // when
        // then
        assertThatThrownBy(
                () -> envelopeValidator.assertEnvelopeContainsOcrDataIfRequired(envelope)
        )
                .isInstanceOf(OcrDataNotFoundException.class)
                .hasMessage("Missing OCR data");
    }

    @ParameterizedTest
    @EnumSource(
            value = Classification.class,
            names = {"NEW_APPLICATION", "SUPPLEMENTARY_EVIDENCE_WITH_OCR"}
    )
    void assertEnvelopeContainsOcrDataIfRequired_should_throw_if_form_document_has_empty_ocr_data(Classification classification) {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "BULKSCAN",
                "POBOX",
                classification,
                singletonList(scannableItem(InputDocumentType.FORM, new InputOcrData()))
        );

        // when
        // then
        assertThatThrownBy(
                () -> envelopeValidator.assertEnvelopeContainsOcrDataIfRequired(envelope)
        )
                .isInstanceOf(OcrDataNotFoundException.class)
                .hasMessage("Missing OCR data");
    }

    @ParameterizedTest
    @EnumSource(
            value = Classification.class,
            names = {"NEW_APPLICATION", "SUPPLEMENTARY_EVIDENCE_WITH_OCR"}
    )
    void assertEnvelopeContainsOcrDataIfRequired_should_pass_if_sscs_document_has_ocr_data(Classification classification) {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "SSCS",
                "POBOX",
                classification,
                singletonList(scannableItem(InputDocumentType.SSCS1, getOcrData()))
        );

        // when
        // then
        assertDoesNotThrow(() -> envelopeValidator.assertEnvelopeContainsOcrDataIfRequired(envelope));
    }

    @ParameterizedTest
    @EnumSource(
            value = Classification.class,
            names = {"NEW_APPLICATION", "SUPPLEMENTARY_EVIDENCE_WITH_OCR"}
    )
    void assertEnvelopeContainsOcrDataIfRequired_should_throw_if_sscs1_document_has_no_ocr_data(Classification classification) {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "SSCS",
                "POBOX",
                classification,
                singletonList(scannableItem(InputDocumentType.SSCS1, null))
        );

        // when
        // then
        assertThatThrownBy(
                () -> envelopeValidator.assertEnvelopeContainsOcrDataIfRequired(envelope)
        )
                .isInstanceOf(OcrDataNotFoundException.class)
                .hasMessage("Missing OCR data");
    }

    @ParameterizedTest
    @EnumSource(
            value = Classification.class,
            names = {"NEW_APPLICATION", "SUPPLEMENTARY_EVIDENCE_WITH_OCR"}
    )
    void assertEnvelopeContainsOcrDataIfRequired_should_throw_if_sscs1_document_has_empty_ocr_data(Classification classification) {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "SSCS",
                "POBOX",
                classification,
                singletonList(scannableItem(InputDocumentType.SSCS1, new InputOcrData()))
        );

        // when
        // then
        assertThatThrownBy(
                () -> envelopeValidator.assertEnvelopeContainsOcrDataIfRequired(envelope)
        )
                .isInstanceOf(OcrDataNotFoundException.class)
                .hasMessage("Missing OCR data");
    }

    @Test
    void assertEnvelopeHasPdfs_should_pass_for_valid_pdfs() {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "SSCS",
                "POBOX",
                SUPPLEMENTARY_EVIDENCE,
                asList(scannableItem("file1.pdf", "dcn1"), scannableItem("file2.pdf", "dcn2"))
        );

        // when
        // then
        assertDoesNotThrow(() -> envelopeValidator.assertEnvelopeHasPdfs(envelope, asList("file1.pdf", "file2.pdf")));
    }

    @Test
    void assertEnvelopeHasPdfs_should_throw_for_not_declared_pdfs() {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "SSCS",
                "POBOX",
                SUPPLEMENTARY_EVIDENCE,
                singletonList(scannableItem("file2.pdf", "dcn2"))
        );

        // when
        // then
        assertThatThrownBy(
                () -> envelopeValidator.assertEnvelopeHasPdfs(envelope, asList("file1.pdf", "file2.pdf"))
        )
                .isInstanceOf(FileNameIrregularitiesException.class)
                .hasMessage("Not declared PDFs: file1.pdf");
    }

    @Test
    void assertEnvelopeHasPdfs_should_throw_for_missing_pdfs() {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "SSCS",
                "POBOX",
                SUPPLEMENTARY_EVIDENCE,
                asList(
                        scannableItem("file1.pdf", "dcn1"),
                        scannableItem("file2.pdf", "dcn2")
                )
        );

        // when
        // then
        assertThatThrownBy(
                () -> envelopeValidator.assertEnvelopeHasPdfs(envelope, singletonList("file2.pdf"))
        )
                .isInstanceOf(FileNameIrregularitiesException.class)
                .hasMessage("Missing PDFs: file1.pdf");
    }

    @Test
    void assertDocumentControlNumbersAreUnique_should_pass_for_correct_dcns() {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "SSCS",
                "POBOX",
                SUPPLEMENTARY_EVIDENCE,
                asList(
                        scannableItem("file1.pdf", "dcn1"),
                        scannableItem("file2.pdf", "dcn2")
                )
        );

        // when
        // then
        assertDoesNotThrow(() -> envelopeValidator.assertDocumentControlNumbersAreUnique(envelope));
    }

    @Test
    void assertDocumentControlNumbersAreUnique_should_throw_for_duplicate_dcns() {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope(
                "SSCS",
                "POBOX",
                SUPPLEMENTARY_EVIDENCE,
                asList(
                        scannableItem("file1.pdf", "dcn1"),
                        scannableItem("file2.pdf", "dcn1"),
                        scannableItem("file3.pdf", "dcn2"),
                        scannableItem("file4.pdf", "dcn2"),
                        scannableItem("file5.pdf", "dcn3")
                )
        );

        // when
        // then
        assertThatThrownBy(
                () -> envelopeValidator.assertDocumentControlNumbersAreUnique(envelope)
        )
                .isInstanceOf(DuplicateDocumentControlNumbersInEnvelopeException.class)
                .hasMessage("Duplicate DCNs in envelope: dcn1, dcn2");
    }

    @Test
    void assertZipFilenameMatchesWithMetadata_should_pass_for_correct_zip_filename() {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope("SSCS");

        // when
        // then
        assertDoesNotThrow(() -> envelopeValidator.assertZipFilenameMatchesWithMetadata(envelope, ZIP_FILE_NAME));
    }

    @Test
    void assertZipFilenameMatchesWithMetadata_should_throw_for_incorrect_zip_filename() {
        // given
        InputEnvelope envelope = InputEnvelopeCreator.inputEnvelope("SSCS");

        // when
        // then
        assertThatThrownBy(
                () -> envelopeValidator.assertZipFilenameMatchesWithMetadata(envelope, "wrong.zip")
        )
                .isInstanceOf(ZipNameNotMatchingMetaDataException.class)
                .hasMessage("Name of the uploaded zip file does not match with field \"zip_file_name\" in the metadata");
    }

    private InputOcrData getOcrData() {
        InputOcrData ocrData = new InputOcrData();
        ocrData.setFields(singletonList(new InputOcrDataField(new TextNode("foo"), new TextNode("bar"))));
        return ocrData;
    }
}
