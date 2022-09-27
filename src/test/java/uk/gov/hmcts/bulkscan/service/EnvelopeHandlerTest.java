package uk.gov.hmcts.bulkscan.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.bulkscan.processor.EnvelopeProcessor;
import uk.gov.hmcts.bulkscan.type.IServiceOcrValidator;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.type.OcrValidationWarnings;
import uk.gov.hmcts.bulkscan.validation.BulkScanOcrValidator;
import uk.gov.hmcts.bulkscan.validation.EnvelopeValidator;

import java.util.List;
import java.util.Optional;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.bulkscan.type.Classification.NEW_APPLICATION;

@ExtendWith(MockitoExtension.class)
class EnvelopeHandlerTest {

    private static final String FILE_NAME = "file1.zip";
    private static final String RESCAN_FOR_FILE_NAME = "file2.zip";
    private static final String DCN = "dcn";
    private static final String POBOX = "pobox";
    private static final String BULKSCAN = "bulkscan";
    private static final String CASE_NUMBER = "case_number";
    private static final String CASE_REFERENCE = "case_reference";

    @Mock
    private EnvelopeValidator envelopeValidator;

    @Mock
    private EnvelopeProcessor envelopeProcessor;

    @Mock
    private BulkScanOcrValidator ocrValidator;

    @Mock
    private IServiceOcrValidator serviceOcrValidator;

    private List<String> pdfs = emptyList();

    private Optional<OcrValidationWarnings> warnings =
            Optional.of(new OcrValidationWarnings(DCN, emptyList()));

    private InputEnvelope inputEnvelope;

    private EnvelopeHandler envelopeHandler;

    @BeforeEach
    void setUp() {
        this.serviceOcrValidator = Mockito.mock(IServiceOcrValidator.class);
        envelopeHandler = new EnvelopeHandler(
                envelopeValidator,
                ocrValidator
        );
        inputEnvelope = new InputEnvelope(
                POBOX,
                BULKSCAN,
                now(),
                now(),
                now(),
                FILE_NAME,
                RESCAN_FOR_FILE_NAME,
                CASE_NUMBER,
                CASE_REFERENCE,
                NEW_APPLICATION,
                emptyList(),
                emptyList(),
                emptyList());
    }

    @Test
    void should_handle_and_save_envelope() {
        given(ocrValidator.assertOcrDataIsValid(inputEnvelope, this.serviceOcrValidator)).willReturn(warnings);

        // when
        envelopeHandler.handleEnvelope(
                FILE_NAME,
                pdfs,
                inputEnvelope,
                this.serviceOcrValidator
        );

        // then
        verify(envelopeValidator).assertZipFilenameMatchesWithMetadata(inputEnvelope, FILE_NAME);
        verify(envelopeValidator).assertEnvelopeContainsOcrDataIfRequired(inputEnvelope);
        verify(envelopeValidator).assertEnvelopeHasPdfs(inputEnvelope, pdfs);
        verify(envelopeValidator).assertDocumentControlNumbersAreUnique(inputEnvelope);
        verify(envelopeValidator).assertEnvelopeContainsDocsOfAllowedTypesOnly(inputEnvelope);

        verifyNoMoreInteractions(envelopeProcessor);
    }
}
