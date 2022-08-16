package uk.gov.hmcts.bulkscan.type;

import java.util.Arrays;
import java.util.List;

public class ZipFileContentDetail {

    private final byte[] metadata;

    public final List<String> pdfFileNames;

    public ZipFileContentDetail(byte[] metadata, List<String> pdfFileNames) {
        this.metadata = Arrays.copyOf(metadata, metadata.length);
        this.pdfFileNames = List.copyOf(pdfFileNames);
    }

    public byte[] getMetadata() {
        return Arrays.copyOf(metadata, metadata.length);
    }
}
