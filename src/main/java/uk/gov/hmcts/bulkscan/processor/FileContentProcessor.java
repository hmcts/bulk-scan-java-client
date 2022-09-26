package uk.gov.hmcts.bulkscan.processor;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.bulkscan.service.EnvelopeHandler;
import uk.gov.hmcts.bulkscan.type.BulkScanEnvelope;
import uk.gov.hmcts.bulkscan.type.IServiceOcrValidator;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.type.ProcessedEnvelopeContents;
import uk.gov.hmcts.bulkscan.type.ZipFileContentDetail;

import java.io.IOException;
import java.util.zip.ZipInputStream;

import static java.util.stream.Collectors.joining;

@Component
public class FileContentProcessor {
    private static final Logger log = LoggerFactory.getLogger(FileContentProcessor.class);

    private final ZipFileProcessor zipFileProcessor;

    private final EnvelopeProcessor envelopeProcessor;

    private final EnvelopeHandler envelopeHandler;

    public FileContentProcessor(
        ZipFileProcessor zipFileProcessor,
        EnvelopeProcessor envelopeProcessor,
        EnvelopeHandler envelopeHandler
    ) {
        this.zipFileProcessor = zipFileProcessor;
        this.envelopeProcessor = envelopeProcessor;
        this.envelopeHandler = envelopeHandler;
    }

    public ProcessedEnvelopeContents processZipFileContent(
        ZipInputStream zis,
        BulkScanEnvelope envelope,
        String serviceName,
        IServiceOcrValidator ocrValidator
    ) throws IOException, ProcessingException {
        var zipFilename = envelope.getFileName();
        ZipFileContentDetail zipDetail = zipFileProcessor.getZipContentDetail(zis, zipFilename);

        InputEnvelope inputEnvelope = envelopeProcessor.parseEnvelope(zipDetail.getMetadata(), zipFilename);

        log.info(
            "Parsed envelope. File name: {}. Container: {}. Payment DCNs: {}. Document DCNs: {}, caseNumber {}",
            zipFilename,
            serviceName,
            inputEnvelope.payments.stream().map(payment -> payment.documentControlNumber).collect(joining(",")),
            inputEnvelope.scannableItems.stream().map(doc -> doc.documentControlNumber).collect(joining(",")),
            inputEnvelope.caseNumber
        );

        var warnings = envelopeHandler.handleEnvelope(
            zipFilename,
            zipDetail.pdfFileNames,
            inputEnvelope,
            ocrValidator
        );

        return new ProcessedEnvelopeContents(envelope, zipDetail, inputEnvelope, warnings, zipFileProcessor);
    }
}

