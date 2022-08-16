package uk.gov.hmcts.bulkscan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.bulkscan.client.BulkScanClientApi;
import uk.gov.hmcts.bulkscan.enums.EnvelopeProcessStatus;
import uk.gov.hmcts.bulkscan.exception.BulkScanServerException;
import uk.gov.hmcts.bulkscan.exception.EnvelopeRejectionException;
import uk.gov.hmcts.bulkscan.processor.FileContentProcessor;
import uk.gov.hmcts.bulkscan.type.BulkScanEnvelope;
import uk.gov.hmcts.bulkscan.type.BulkScanEnvelopesResponse;
import uk.gov.hmcts.bulkscan.type.EnvelopeProcessAttempt;
import uk.gov.hmcts.bulkscan.type.IEnvelopeReceiver;
import uk.gov.hmcts.bulkscan.type.IServiceOcrValidator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import static java.util.Collections.emptyList;

@Service
@Slf4j
public class BulkScanClientService {

    private final BulkScanClientApi bulkScanClient;
    private final FileContentProcessor fileContentProcessor;
    private final IEnvelopeReceiver envelopeReceiver;
    private final AuthTokenGenerator authTokenGenerator;
    private final IServiceOcrValidator ocrValidator;

    public BulkScanClientService(
        BulkScanClientApi bulkScanClient,
        FileContentProcessor fileContentProcessor,
        IEnvelopeReceiver envelopeReceiver,
        AuthTokenGenerator authTokenGenerator,
        IServiceOcrValidator ocrValidator
    ) {
        this.bulkScanClient = bulkScanClient;
        this.fileContentProcessor = fileContentProcessor;
        this.envelopeReceiver = envelopeReceiver;
        this.authTokenGenerator = authTokenGenerator;
        this.ocrValidator = ocrValidator;
    }

    public List<BulkScanEnvelope> getNewScanList(String serviceName) {
        log.info("Checking for new scanned files...");
        var envelopeResponse = bulkScanClient.getPendingEnvelopes(authTokenGenerator.generate(), serviceName);
        if (!envelopeResponse.getStatusCode().equals(HttpStatus.OK)) {
            throw new BulkScanServerException(
                String.format("Error retrieving new scanned files. HTTP Response %s: {}, Body: %s",
                    envelopeResponse.getStatusCode(), envelopeResponse.getBody())
            );
        }

        return Optional.ofNullable(envelopeResponse.getBody())
            .map(BulkScanEnvelopesResponse::getData)
            .orElse(emptyList());
    }

    public void processEnvelopes(
        String serviceName,
        List<BulkScanEnvelope> envelopes
    ) {

        envelopes.forEach(envelope -> {
            log.info("Processing file '{}'", envelope.getFileName());
            log.info("Url: {}", envelope.getUrl());

            try {
                URL url = new URL(envelope.getUrl());

                try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(url.openStream(), 1024))) {

                    var processedEnvelope = fileContentProcessor.processZipFileContent(
                        zis,
                        envelope,
                        serviceName,
                        ocrValidator
                    );

                    zis.closeEntry();
                    zis.close();

                    var response = envelopeReceiver.onEnvelopeReceived(processedEnvelope);

                    recordProcessingAttempt(
                        authTokenGenerator.generate(),
                        new EnvelopeProcessAttempt(
                            UUID.randomUUID(),
                            response.envelopeETag(),
                            serviceName,
                            new Date().toString(),
                            response.description(),
                            emptyList(),
                            emptyList(),
                            response.status()
                        )
                    );
                }

            } catch (EnvelopeRejectionException e) {
                log.warn("Rejected file {} from container {} - invalid", envelope.getFileName(), serviceName, e);
                recordProcessingAttempt(
                    authTokenGenerator.generate(),
                    new EnvelopeProcessAttempt(
                        UUID.randomUUID(),
                        envelope.getEtag(),
                        serviceName,
                        new Date().toString(),
                        "Rejected Envelope " + e.getClass().getName(),
                        emptyList(),
                        List.of(e.getMessage()),
                        EnvelopeProcessStatus.ERRORS
                    )
                );
            } catch (MalformedURLException e) {
                log.error("Url '{}' is malformed.", envelope.getUrl());
                log.error(e.getMessage());
                log.error("Failed to process file {} from container {}", envelope.getFileName(), serviceName, e);
                recordProcessingAttempt(
                    authTokenGenerator.generate(),
                    new EnvelopeProcessAttempt(
                        UUID.randomUUID(),
                        envelope.getEtag(),
                        serviceName,
                        new Date().toString(),
                        "Bad url for retrieving envelope "  + envelope.getUrl(),
                        emptyList(),
                        List.of(e.getMessage()),
                        EnvelopeProcessStatus.FATAL
                    )
                );
            } catch (Exception e) {
                log.error(e.getMessage());
                log.error("Failed to process file {} from container {}", envelope.getFileName(), serviceName, e);
                recordProcessingAttempt(
                    authTokenGenerator.generate(),
                    new EnvelopeProcessAttempt(
                        UUID.randomUUID(),
                        envelope.getEtag(),
                        serviceName,
                        new Date().toString(),
                        "Error processing Envelope",
                        emptyList(),
                        List.of(e.getMessage()),
                        EnvelopeProcessStatus.FATAL
                    )
                );
            }
        });
    }

    public void recordProcessingAttempt(String serviceAuthToken, EnvelopeProcessAttempt processAttempt) {
        log.info("Recording Processing Attempt of Envelope {}", processAttempt.envelopeId);
        var response = bulkScanClient.recordEnvelopeProcessingAttempt(
            serviceAuthToken,
            processAttempt.serviceName,
            processAttempt.envelopeId,
            processAttempt.attemptId,
            processAttempt
        );
        if (!response.getStatusCode().equals(HttpStatus.OK)) {
            log.info("Successfully recorded processing attempt of Envelope {} Attempt Id {}",
                processAttempt.envelopeId,
                processAttempt.attemptId
            );
        }
    }
}
