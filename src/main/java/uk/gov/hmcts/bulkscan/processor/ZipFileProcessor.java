package uk.gov.hmcts.bulkscan.processor;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.bulkscan.exception.FileSizeExceedMaxUploadLimit;
import uk.gov.hmcts.bulkscan.exception.NonPdfFileFoundException;
import uk.gov.hmcts.bulkscan.type.ZipFileContentDetail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.io.ByteStreams.toByteArray;

@Component
public class ZipFileProcessor {
    private static final long MAX_PDF_SIZE = 314_572_800; //300 mb

    private static final Logger log = LoggerFactory.getLogger(ZipFileProcessor.class);
    public final String downloadPath;

    public ZipFileProcessor(@Value("${tmp-folder-path-for-download}") String downloadPath) {
        this.downloadPath = downloadPath + File.separator;
    }

    public void extractPdfFiles(
        ZipInputStream extractedZis,
        String zipFileName,
        Consumer<List<File>> pdfListConsumer
    ) throws IOException {
        try {
            List<File> fileList = createPdfAndSaveToTemp(extractedZis, zipFileName);
            checkFileSizeAgainstUploadLimit(fileList);
            pdfListConsumer.accept(fileList);
            log.info("Function consumed, zipFileName {}", zipFileName);
        } finally {
            deleteFolder(zipFileName);
        }
    }

    public void checkFileSizeAgainstUploadLimit(List<File> fileList) {
        long totalSize = 0;
        for (File file : fileList) {
            long fileSize = file.length();
            if (fileSize > MAX_PDF_SIZE) {
                log.info("PDF size exceeds the max upload size limit, {} {} ", file.getName(), fileSize);
                throw new FileSizeExceedMaxUploadLimit("Pdf size =" + fileSize
                    + " exceeds the max limit=" + MAX_PDF_SIZE);
            }
            totalSize += fileSize;
        }
        log.info("Total upload size {}", totalSize);
    }

    private void deleteFolder(String zipFileName) {
        String folderPath =  downloadPath +  zipFileName;
        try {
            FileUtils.deleteDirectory(new File(folderPath));
            log.info("Folder deleted {}", folderPath);
        } catch (IOException e) {
            log.error("Folder delete unsuccessful, path: {} ", folderPath, e);
        }
    }

    public ZipFileContentDetail getZipContentDetail(
        ZipInputStream extractedZis,
        String zipFileName
    ) throws IOException {

        ZipEntry zipEntry;

        List<String> pdfs = new ArrayList<>();
        byte[] metadata = null;

        while ((zipEntry = extractedZis.getNextEntry()) != null) {
            if (!zipEntry.isDirectory()) {
                switch (FilenameUtils.getExtension(zipEntry.getName())) {
                    case "json" -> {
                        metadata = toByteArray(extractedZis);
                        log.info(
                            "File: {}, Meta data size: {}",
                            zipFileName,
                            FileUtils.byteCountToDisplaySize(metadata.length)
                        );
                    }
                    case "pdf" -> pdfs.add(zipEntry.getName());
                    default ->
                        // contract breakage
                        throw new NonPdfFileFoundException(zipFileName, zipEntry.getName());
                }
            }
        }

        log.info("PDFs found in {}: {}", zipFileName, pdfs.size());

        return new ZipFileContentDetail(metadata, pdfs);
    }

    private List<File> createPdfAndSaveToTemp(
        ZipInputStream extractedZis,
        String zipFileName
    ) throws IOException {

        ZipEntry zipEntry;
        List<File> pdfs = new ArrayList<>();
        String folderPath =  downloadPath + zipFileName;

        while ((zipEntry = extractedZis.getNextEntry()) != null) {
            if ("pdf".equals(FilenameUtils.getExtension(zipEntry.getName()))) {
                String filePath =
                    folderPath + File.separator + FilenameUtils.getName(zipEntry.getName());
                var pdfFile = new File(filePath);
                FileUtils.copyToFile(extractedZis, pdfFile);
                pdfs.add(pdfFile);
                log.info(
                    "ZipFile:{}, has {}, pdf size: {}",
                    zipFileName,
                    zipEntry.getName(),
                    FileUtils.byteCountToDisplaySize(Files.size(pdfFile.toPath()))
                );
            }
        }
        log.info("Zip file {} has {} pdfs: {}. Saved to {} ", zipFileName, pdfs.size(), pdfs, folderPath);

        return ImmutableList.copyOf(pdfs);
    }
}
