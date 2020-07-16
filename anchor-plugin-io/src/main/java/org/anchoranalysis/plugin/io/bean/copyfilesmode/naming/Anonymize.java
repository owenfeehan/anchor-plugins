/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.functional.OptionalUtilities;
import org.anchoranalysis.core.text.TypedValue;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.output.csv.CSVWriter;
import org.apache.commons.io.FilenameUtils;

/**
 * Copies files to a number 001 002 etc. in the same order they are inputted.
 *
 * <p>No shuffling occurs.
 *
 * @author feehano
 */
public class Anonymize implements CopyFilesNaming {

    private static final String OUTPUT_CSV_FILENAME = "output.csv";

    @Value
    private static class FileMapping {
        private final Path original;
        private final String anonymized;
        private final int iter;
    }

    // START BEAN PROPERTIES
    /**
     * Iff TRUE, a mapping.csv file is created showing the mapping between the original-names and
     * the anonymized versions
     */
    @BeanField @Getter @Setter private boolean outputCSV = true;
    // END BEAN PROPERTIES

    private Optional<List<FileMapping>> optionalMappings;
    private String formatStr;

    @Override
    public void beforeCopying(Path destDir, int totalNumFiles) {
        optionalMappings = OptionalUtilities.createFromFlag(outputCSV, ArrayList::new);

        formatStr = createFormatStrForMaxNum(totalNumFiles);
    }

    @Override
    public Optional<Path> destinationPathRelative(Path sourceDir, Path destDir, File file, int iter)
            throws AnchorIOException {
        String ext = FilenameUtils.getExtension(file.toString());
        String fileNameNew = createNumericString(iter) + "." + ext;

        if (optionalMappings.isPresent()) {
            addMapping(optionalMappings.get(), sourceDir, file, iter, fileNameNew);
        }

        return Optional.of(Paths.get(fileNameNew));
    }

    @Override
    public void afterCopying(Path destDir, boolean dummyMode) throws AnchorIOException {

        if (optionalMappings.isPresent() && !dummyMode) {
            writeOutputCSV(optionalMappings.get(), destDir);
            optionalMappings = Optional.empty();
        }
    }

    private void addMapping(
            List<FileMapping> mapping, Path sourceDir, File file, int iter, String fileNameNew)
            throws AnchorIOException {
        synchronized (this) {
            mapping.add( // NOSONAR
                    new FileMapping(
                            NamingUtilities.filePathDiff(sourceDir, file.toPath()),
                            fileNameNew,
                            iter));
        }
    }

    private void writeOutputCSV(List<FileMapping> listMappings, Path destDir)
            throws AnchorIOException {

        Path csvOut = destDir.resolve(OUTPUT_CSV_FILENAME);

        CSVWriter csvWriter = CSVWriter.create(csvOut);

        csvWriter.writeHeaders(Arrays.asList("iter", "in", "out"));

        try {
            for (FileMapping mapping : listMappings) {
                csvWriter.writeRow(
                        Arrays.asList(
                                new TypedValue(mapping.getIter()),
                                new TypedValue(mapping.getOriginal().toString()),
                                new TypedValue(mapping.getAnonymized())));
            }
        } finally {
            csvWriter.close();
        }
    }

    private String createNumericString(int iter) {
        return String.format(formatStr, iter);
    }

    private static String createFormatStrForMaxNum(int maxNum) {
        int maxNumDigits = (int) Math.ceil(Math.log10(maxNum));

        if (maxNumDigits > 0) {
            return "%0" + maxNumDigits + "d";
        } else {
            return "%d";
        }
    }
}
