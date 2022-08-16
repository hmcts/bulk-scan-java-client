package uk.gov.hmcts.bulkscan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.bulkscan.exception.BulkScanServerException;
import uk.gov.hmcts.bulkscan.service.BulkScanClientService;

@Component
@Slf4j
public class ProcessScannedDocumentsTask implements Runnable {

    private final BulkScanClientService bulkscanBulkScanClientService;

    private final String serviceName;

    public ProcessScannedDocumentsTask(BulkScanClientService bulkscanBulkScanClientService,
                                       @Value("${bulk-scan.service-name}") String serviceName) {
        this.bulkscanBulkScanClientService = bulkscanBulkScanClientService;
        this.serviceName = serviceName;
    }


    @Override
    @Scheduled(fixedDelay = 1000)
    public void run() {
        log.info("ProcessScannedDocumentsTask scheduled task started");

        try {
            bulkscanBulkScanClientService.processEnvelopes(
                serviceName,
                bulkscanBulkScanClientService.getNewScanList(serviceName)
            );

        } catch (BulkScanServerException e) {
            log.error(e.getMessage());
        }

        log.info("ProcessScannedDocumentsTask scheduled task complete.");
    }
}
