package uk.gov.hmcts.bulkscan.type;

public interface IEnvelopeReceiver {

    BulkScanEnvelopeProcessingResponse onEnvelopeReceived(ProcessedEnvelopeContents envelopeContents);
}
