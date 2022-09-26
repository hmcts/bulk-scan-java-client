package uk.gov.hmcts.bulkscan.type;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.bulkscan.processor.ZipFileProcessor;

import java.io.BufferedInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipInputStream;

@Slf4j
public class ProcessedEnvelopeContents {

    private final BulkScanEnvelope envelope;
    private final ZipFileContentDetail zipDetail;
    private final InputEnvelope inputEnvelope;
    private final Optional<OcrValidationWarnings> warnings;
    private final ZipFileProcessor zipFileProcessor;
    private Map<String, File> extractedFiles;

    public ProcessedEnvelopeContents(BulkScanEnvelope envelope,
                                     ZipFileContentDetail zipDetail,
                                     InputEnvelope inputEnvelope,
                                     Optional<OcrValidationWarnings> warnings,
                                     ZipFileProcessor zipFileProcessor) {
        this.envelope = envelope;
        this.zipDetail = zipDetail;
        this.inputEnvelope = inputEnvelope;
        this.warnings = warnings;
        this.zipFileProcessor = zipFileProcessor;
        this.extractedFiles = Collections.<String, File>emptyMap();
    }

    public BulkScanEnvelope getEnvelope() {
        return envelope;
    }

    public ZipFileContentDetail getZipDetail() {
        return zipDetail;
    }

    public InputEnvelope getInputEnvelope() {
        return inputEnvelope;
    }

    public Optional<OcrValidationWarnings> getWarnings() {
        return warnings;
    }

    public Map<String, File> getExtractedFiles() throws MalformedURLException {

        if (!extractedFiles.isEmpty()) {
            return extractedFiles;
        }

        URL url = new URL(envelope.getUrl());
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(url.openStream(), 1024))) {
            extractedFiles = zipFileProcessor.extractPdfFiles(zis, inputEnvelope.zipFileName);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return extractedFiles;
    }


}
