package uk.gov.hmcts.bulkscan;


import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import uk.gov.hmcts.bulkscan.exception.BulkScanServerException;
import uk.gov.hmcts.bulkscan.service.BulkScanClientService;

import java.util.Collections;

public class ProcessScannedDocumentsTaskTest {

    @InjectMocks
    private BulkScanClientService bulkscanBulkScanClientService;

    @Test
    public void shouldGetNewScanListAndProcessIt() {
        var serviceName = "test";
        bulkscanBulkScanClientService = Mockito.mock(BulkScanClientService.class);
        var task = new ProcessScannedDocumentsTask(bulkscanBulkScanClientService, serviceName);
        task.run();
        Mockito.verify(bulkscanBulkScanClientService, Mockito.times(1))
                .getNewScanList(serviceName);
        Mockito.verify(bulkscanBulkScanClientService, Mockito.times(1))
                .processEnvelopes(serviceName, bulkscanBulkScanClientService.getNewScanList(serviceName));
    }

    @Test
    public void shouldLogException() {
        var serviceName = "test";
        bulkscanBulkScanClientService = Mockito.mock(BulkScanClientService.class);
        Mockito.when(bulkscanBulkScanClientService.getNewScanList(serviceName))
                .thenThrow(BulkScanServerException.class);

        var task = new ProcessScannedDocumentsTask(bulkscanBulkScanClientService, serviceName);
        task.run();
        Mockito.verify(bulkscanBulkScanClientService, Mockito.times(1))
                .getNewScanList(serviceName);
        Mockito.verify(bulkscanBulkScanClientService, Mockito.times(0))
                .processEnvelopes(serviceName, Collections.emptyList());
    }
}
