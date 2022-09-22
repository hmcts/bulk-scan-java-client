package uk.gov.hmcts.bulkscan.type;

import uk.gov.hmcts.bulkscan.enums.EnvelopeProcessStatus;

import java.io.File;
import java.util.List;

public interface IPdfProcessor {

    EnvelopeProcessStatus processPdfList(List<File> pdfFiles);
}
