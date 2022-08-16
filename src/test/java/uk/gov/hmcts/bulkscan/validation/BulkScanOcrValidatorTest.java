package uk.gov.hmcts.bulkscan.validation;

import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.bulkscan.exception.OcrPresenceException;
import uk.gov.hmcts.bulkscan.exception.OcrValidationException;
import uk.gov.hmcts.bulkscan.type.Classification;
import uk.gov.hmcts.bulkscan.type.IServiceOcrValidator;
import uk.gov.hmcts.bulkscan.type.InputDocumentType;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.type.InputOcrData;
import uk.gov.hmcts.bulkscan.type.InputOcrDataField;
import uk.gov.hmcts.bulkscan.type.InputScannableItem;
import uk.gov.hmcts.bulkscan.type.OcrValidationResult;
import uk.gov.hmcts.bulkscan.type.OcrValidationStatus;
import uk.gov.hmcts.bulkscan.type.OcrValidationWarnings;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.bulkscan.helper.InputEnvelopeCreator.inputEnvelope;
import static uk.gov.hmcts.bulkscan.type.Classification.SUPPLEMENTARY_EVIDENCE;
import static uk.gov.hmcts.bulkscan.type.Classification.SUPPLEMENTARY_EVIDENCE_WITH_OCR;
import static uk.gov.hmcts.bulkscan.type.InputDocumentType.FORM;
import static uk.gov.hmcts.bulkscan.type.InputDocumentType.OTHER;
import static uk.gov.hmcts.bulkscan.type.InputDocumentType.SSCS1;

@ExtendWith(MockitoExtension.class)
class BulkScanOcrValidatorTest {

    private static final String PO_BOX_1 = "sample PO box 1";
    private static final String PO_BOX_2 = "sample PO box 2";

    @Mock private OcrPresenceValidator presenceValidator;

    @Mock private IServiceOcrValidator serviceOcrValidator;

    private BulkScanOcrValidator ocrValidator;

    @BeforeEach
    void setUp() {
        this.ocrValidator = new BulkScanOcrValidator(presenceValidator);
        this.serviceOcrValidator = Mockito.mock(IServiceOcrValidator.class);
    }

    @Test
    void should_validate_the_presence_of_ocr_data() {
        // given
        List<InputScannableItem> docs =
                asList(
                        doc(OTHER, "other", sampleOcr()),
                        doc(OTHER, "other", null)
                );
        InputEnvelope envelope =
                inputEnvelope(
                        "BULKSCAN",
                        PO_BOX_1,
                        SUPPLEMENTARY_EVIDENCE_WITH_OCR,
                        docs
                );
        given(presenceValidator.assertHasProperlySetOcr(docs))
                .willThrow(new OcrPresenceException("msg"));

        // when
        Throwable exc = catchThrowable(() ->
                ocrValidator.assertOcrDataIsValid(envelope, this.serviceOcrValidator));

        // then
        assertThat(exc)
                .isInstanceOf(OcrPresenceException.class)
                .hasMessage("msg");
    }

    @Test
    void should_handle_sscs1_forms_without_subtype() {
        InputScannableItem docWithOcr = doc(SSCS1, null, sampleOcr());
        List<InputScannableItem> docs =
                asList(
                        docWithOcr,
                        doc(OTHER, "other", null)
                );
        InputEnvelope envelope = inputEnvelope(
                "BULKSCAN",
                PO_BOX_1,
                SUPPLEMENTARY_EVIDENCE,
                docs
        );

        given(this.serviceOcrValidator.validateEnvelope(any(), any()))
                .willReturn(new OcrValidationResult(OcrValidationStatus.SUCCESS, emptyList(), emptyList()));

        given(presenceValidator.assertHasProperlySetOcr(envelope.scannableItems))
                .willReturn(Optional.of(docWithOcr));

        ocrValidator.assertOcrDataIsValid(envelope, this.serviceOcrValidator);
    }

    @Test
    void should_return_warnings_from_successful_validation_result() {
        // given
        List<String> expectedWarnings = ImmutableList.of("warning 1", "warning 2");

        InputScannableItem scannableItem = doc(FORM, "subtype1", sampleOcr());
        InputEnvelope envelope = envelope(
                PO_BOX_1,
                singletonList(scannableItem),
                SUPPLEMENTARY_EVIDENCE
        );

        given(this.serviceOcrValidator.validateEnvelope(any(), any()))
                .willReturn(new OcrValidationResult(OcrValidationStatus.WARNINGS, expectedWarnings, emptyList()));

        given(presenceValidator.assertHasProperlySetOcr(envelope.scannableItems))
                .willReturn(Optional.of(scannableItem));

        // when
        Optional<OcrValidationWarnings> warnings = ocrValidator.assertOcrDataIsValid(envelope, this.serviceOcrValidator);

        // then
        assertThat(warnings).isPresent();
        assertThat(warnings.get().documentControlNumber).isEqualTo(scannableItem.documentControlNumber);
        assertThat(warnings.get().warnings).isEqualTo(expectedWarnings);
    }

    @Test
    void should_not_call_validation_there_are_no_documents_with_ocr() {
        // given
        InputEnvelope envelope = envelope(
                PO_BOX_1,
                asList(
                        doc(OTHER, "other", null),
                        doc(OTHER, "other", null)
                ),
                SUPPLEMENTARY_EVIDENCE
        );

        // when
        ocrValidator.assertOcrDataIsValid(envelope, this.serviceOcrValidator);

        // then
        verify(this.serviceOcrValidator, never()).validateEnvelope(any(), any());
    }

    @Test
    void should_throw_an_exception_if_service_responded_with_error_response() {
        // given
        InputEnvelope envelope = envelope(
                PO_BOX_1,
                asList(
                        doc(FORM, "y", sampleOcr()),
                        doc(OTHER, "other", null)
                ),
                SUPPLEMENTARY_EVIDENCE
        );

        given(presenceValidator.assertHasProperlySetOcr(any()))
                .willReturn(Optional.of(doc(FORM, "y", sampleOcr())));

        given(this.serviceOcrValidator.validateEnvelope(any(), any()))
                .willReturn(new OcrValidationResult(OcrValidationStatus.ERRORS, emptyList(), singletonList("Error!")));

        // when
        Throwable err = catchThrowable(() -> ocrValidator.assertOcrDataIsValid(envelope, this.serviceOcrValidator));


        // then
        assertThat(err)
                .isInstanceOf(OcrValidationException.class)
                .hasMessageContaining("OCR validation service returned OCR-specific errors");
    }

    private InputOcrData sampleOcr() {
        InputOcrData data = new InputOcrData();
        data.setFields(asList(
                new InputOcrDataField(new TextNode("hello"), new TextNode("world")),
                new InputOcrDataField(new TextNode("foo"), new TextNode("bar"))
        ));
        return data;
    }

    private InputEnvelope envelope(
            String poBox,
            List<InputScannableItem> scannableItems,
            Classification classification
    ) {
        return inputEnvelope(
                "BULKSCAN",
                poBox,
                classification,
                scannableItems
        );
    }

    private InputScannableItem doc(InputDocumentType docType, String subtype, InputOcrData ocrData) {
        return new InputScannableItem(
                UUID.randomUUID().toString(),
                Instant.now(),
                null,
                null,
                null,
                null,
                ocrData,
                null,
                null,
                docType,
                subtype
        );
    }
}