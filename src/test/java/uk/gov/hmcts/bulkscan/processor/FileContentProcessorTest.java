package uk.gov.hmcts.bulkscan.processor;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.bulkscan.service.EnvelopeHandler;
import uk.gov.hmcts.bulkscan.type.BulkScanEnvelope;
import uk.gov.hmcts.bulkscan.type.IServiceOcrValidator;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.type.ZipFileContentDetail;
import uk.gov.hmcts.bulkscan.validation.BulkScanOcrValidator;

import java.util.List;
import java.util.zip.ZipInputStream;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.bulkscan.type.Classification.NEW_APPLICATION;

@ExtendWith(MockitoExtension.class)
class FileContentProcessorTest {
    private static final String FILE_NAME = "file1.zip";
    private static final String CONTAINER_NAME = "container";
    private static final String POBOX = "pobox";
    private static final String BULKSCAN = "bulkscan";
    private static final String CASE_NUMBER = "case_number";
    private static final String CASE_REFERENCE = "case_reference";

    @Mock
    private EnvelopeProcessor envelopeProcessor;

    @Mock
    private ZipFileProcessor zipFileProcessor;

    @Mock
    private EnvelopeHandler envelopeHandler;

    @Mock
    private ZipInputStream zis;

    @Mock
    private BulkScanOcrValidator ocrValidator;

    @Mock
    private IServiceOcrValidator serviceOcrValidator;

    private byte[] metadata = new byte[]{};

    private List<String> pdfs = emptyList();

    private ZipFileContentDetail zipFileContentDetail = new ZipFileContentDetail(new byte[]{}, emptyList());

    private InputEnvelope inputEnvelope;

    private BulkScanEnvelope bulkScanEnvelope;

    private FileContentProcessor fileContentProcessor;

    @BeforeEach
    void setUp() {
        fileContentProcessor = new FileContentProcessor(
                zipFileProcessor,
                envelopeProcessor,
                envelopeHandler
        );
        serviceOcrValidator = Mockito.mock(IServiceOcrValidator.class);
        bulkScanEnvelope = new BulkScanEnvelope(
                "etag",
                FILE_NAME,
                "https://thisisaurl.com/" + FILE_NAME,
                "2022-05-09T11:47:28Z",
                128L,
                ContentType.BINARY.toString()
        );
        inputEnvelope = new InputEnvelope(
                POBOX,
                BULKSCAN,
                now(),
                now(),
                now(),
                FILE_NAME,
                null,
                CASE_NUMBER,
                CASE_REFERENCE,
                NEW_APPLICATION,
                emptyList(),
                emptyList(),
                emptyList());
    }

    @Test
    void should_process_file_content_and_save_envelope() throws Exception {
        // given
        given(zipFileProcessor.getZipContentDetail(zis, FILE_NAME)).willReturn(zipFileContentDetail);
        given(envelopeProcessor.parseEnvelope(metadata, FILE_NAME)).willReturn(inputEnvelope);

        // when
        fileContentProcessor.processZipFileContent(
                zis,
                bulkScanEnvelope,
                CONTAINER_NAME,
                serviceOcrValidator
        );

        // then
        verify(envelopeHandler).handleEnvelope(
                FILE_NAME,
                pdfs,
                inputEnvelope,
                serviceOcrValidator
        );
        verifyNoMoreInteractions(envelopeProcessor);
    }
}