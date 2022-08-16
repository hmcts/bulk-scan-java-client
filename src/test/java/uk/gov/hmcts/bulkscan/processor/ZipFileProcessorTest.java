package uk.gov.hmcts.bulkscan.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.bulkscan.helper.DirectoryZipper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ZipFileProcessorTest {

    private static final String FOLDER_NAME = "tempwork";
    ZipFileProcessor  zipFileProcessor = new ZipFileProcessor(FOLDER_NAME);

    @Test
    void should_run_provided_function_when_there_is_no_error() throws IOException {
        byte[] zipFile = DirectoryZipper.zipDir("envelopes/sample_valid_content");

        ZipInputStream extractedZis = new ZipInputStream(new ByteArrayInputStream(zipFile));

        var zipFileName = "1_2324_43543.zip";
        var consumer = mock(Consumer.class);
        zipFileProcessor.extractPdfFiles(extractedZis, zipFileName, consumer);
        verify(consumer).accept(any());
        assertThat(new File(FOLDER_NAME + File.separator + zipFileName)).doesNotExist();
    }
}
