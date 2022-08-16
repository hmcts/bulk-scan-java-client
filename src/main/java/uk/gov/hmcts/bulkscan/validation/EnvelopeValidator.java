package uk.gov.hmcts.bulkscan.validation;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.bulkscan.exception.DisallowedDocumentTypesException;
import uk.gov.hmcts.bulkscan.exception.DuplicateDocumentControlNumbersInEnvelopeException;
import uk.gov.hmcts.bulkscan.exception.FileNameIrregularitiesException;
import uk.gov.hmcts.bulkscan.exception.OcrDataNotFoundException;
import uk.gov.hmcts.bulkscan.exception.ZipNameNotMatchingMetaDataException;
import uk.gov.hmcts.bulkscan.type.Classification;
import uk.gov.hmcts.bulkscan.type.InputDocumentType;
import uk.gov.hmcts.bulkscan.type.InputEnvelope;
import uk.gov.hmcts.bulkscan.type.InputScannableItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class EnvelopeValidator {

    private static final InputDocumentType defaultOcrDocumentType = InputDocumentType.FORM;
    private static final Map<String, InputDocumentType> ocrDocumentTypePerJurisdiction =
        Map.of(
            "SSCS", InputDocumentType.SSCS1
        );

    private static final Map<Classification, List<InputDocumentType>> disallowedDocumentTypes =
        Map.of(
            Classification.EXCEPTION, emptyList(),
            Classification.NEW_APPLICATION, emptyList(),
            Classification.SUPPLEMENTARY_EVIDENCE, asList(InputDocumentType.FORM, InputDocumentType.SSCS1),
            Classification.SUPPLEMENTARY_EVIDENCE_WITH_OCR, emptyList()
        );

    /**
     * Assert envelope contains only scannable items of types that are allowed for the envelope's classification.
     * Otherwise, throws an exception.
     *
     * @param envelope to assert against
     */
    public void assertEnvelopeContainsDocsOfAllowedTypesOnly(InputEnvelope envelope) {
        List<String> disallowedDocTypesFound =
            envelope
                .scannableItems
                .stream()
                .filter(item -> disallowedDocumentTypes.get(envelope.classification).contains(item.documentType))
                .map(item -> item.documentType.toString())
                .collect(toList());

        if (!disallowedDocTypesFound.isEmpty()) {
            String errorMessage = String.format(
                "Envelope contains scannable item(s) of types that are not allowed for classification '%s': [%s]",
                envelope.classification,
                StringUtils.join(disallowedDocTypesFound, ", ")
            );

            throw new DisallowedDocumentTypesException(errorMessage);
        }
    }

    /**
     * Assert scannable items contain ocr data
     * when envelope classification is NEW_APPLICATION or SUPPLEMENTARY_EVIDENCE_WITH_OCR
     * Throws exception otherwise.
     *
     * @param envelope to assert against
     */
    public void assertEnvelopeContainsOcrDataIfRequired(InputEnvelope envelope) {

        if (envelope.classification == Classification.NEW_APPLICATION
            || envelope.classification == Classification.SUPPLEMENTARY_EVIDENCE_WITH_OCR) {

            List<InputDocumentType> typesThatShouldHaveOcrData =
                Stream.of(
                        defaultOcrDocumentType,
                        ocrDocumentTypePerJurisdiction.get(envelope.jurisdiction)
                    ).filter(Objects::nonNull)
                    .collect(toList());

            List<InputScannableItem> docsThatShouldHaveOcr = envelope
                .scannableItems
                .stream()
                .filter(doc -> typesThatShouldHaveOcrData.contains(doc.documentType))
                .collect(toList());

            if (docsThatShouldHaveOcr.isEmpty()) {
                String types = typesThatShouldHaveOcrData.stream()
                    .map(InputDocumentType::toString).collect(joining(", "));
                throw new OcrDataNotFoundException("No documents of type " + types + " found");
            }

            if (docsThatShouldHaveOcr
                .stream()
                .allMatch(
                    doc -> isNull(doc.ocrData) || isEmpty(doc.ocrData.getFields())
                )
            ) {
                throw new OcrDataNotFoundException("Missing OCR data");
            }
        }
    }

    /**
     * Assert given envelope has scannable items exactly matching
     * the filenames with list of pdfs acquired from zip file.
     * In case there is a mismatch an exception is thrown.
     *
     * @param envelope to assert against
     * @param pdfs     to assert against
     */
    public void assertEnvelopeHasPdfs(InputEnvelope envelope, List<String> pdfs) {
        List<String> problems = new ArrayList<>();

        List<String> duplicateFileNames =
            getDuplicates(envelope.scannableItems.stream().map(it -> it.fileName).collect(toList()));

        if (!duplicateFileNames.isEmpty()) {
            problems.add("Duplicate scanned items file names: " + String.join(", ", duplicateFileNames));
        }

        Set<String> scannedFileNames = envelope
            .scannableItems
            .stream()
            .map(item -> item.fileName)
            .collect(toSet());

        Set<String> pdfFileNames = pdfs.stream().map(filename -> new File(filename).getName()).collect(toSet());

        Set<String> missingActualPdfFiles = Sets.difference(scannedFileNames, pdfFileNames);
        Set<String> notDeclaredPdfs = Sets.difference(pdfFileNames, scannedFileNames);


        if (!notDeclaredPdfs.isEmpty()) {
            problems.add("Not declared PDFs: " + String.join(", ", notDeclaredPdfs));
        }

        if (!missingActualPdfFiles.isEmpty()) {
            problems.add("Missing PDFs: " + String.join(", ", missingActualPdfFiles));
        }

        if (!problems.isEmpty()) {
            throw new FileNameIrregularitiesException(String.join(". ", problems));
        }
    }

    public void assertDocumentControlNumbersAreUnique(InputEnvelope envelope) {
        List<String> dcns = envelope.scannableItems.stream().map(it -> it.documentControlNumber).collect(toList());
        List<String> duplicateDcns = getDuplicates(dcns);
        if (!duplicateDcns.isEmpty()) {
            throw new DuplicateDocumentControlNumbersInEnvelopeException(
                "Duplicate DCNs in envelope: " + String.join(", ", duplicateDcns)
            );
        }
    }

    public void assertZipFilenameMatchesWithMetadata(InputEnvelope envelope, String zipFileName) {
        if (!envelope.zipFileName.equals(zipFileName)) {
            throw new ZipNameNotMatchingMetaDataException(
                "Name of the uploaded zip file does not match with field \"zip_file_name\" in the metadata"
            );
        }
    }

    private List<String> getDuplicates(List<String> collection) {
        return collection
            .stream()
            .collect(groupingBy(it -> it, counting()))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 1)
            .map(Map.Entry::getKey)
            .collect(toList());
    }
}
