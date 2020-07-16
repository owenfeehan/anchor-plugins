/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.groupfiles;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.DefaultInstance;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.io.bean.channel.map.ImgChnlMapCreator;
import org.anchoranalysis.image.io.bean.rasterreader.RasterReader;
import org.anchoranalysis.image.io.input.NamedChnlsInput;
import org.anchoranalysis.io.bean.descriptivename.DescriptiveNameFromFile;
import org.anchoranalysis.io.bean.input.InputManager;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.input.FileInput;
import org.anchoranalysis.io.input.descriptivename.DescriptiveFile;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastFolders;
import org.anchoranalysis.plugin.io.bean.groupfiles.check.CheckParsedFilePathBag;
import org.anchoranalysis.plugin.io.bean.groupfiles.parser.FilePathParser;
import org.anchoranalysis.plugin.io.bean.input.file.Files;
import org.anchoranalysis.plugin.io.multifile.FileDetails;
import org.anchoranalysis.plugin.io.multifile.MultiFileReaderOpenedRaster;
import org.anchoranalysis.plugin.io.multifile.ParsedFilePathBag;

/**
 * An input-manager that can group together files to form stacks or time-series based on finding
 * patterns in the file path (via regular expressions)
 *
 * <p>The manager applies a regular expression on a set of input file paths, and identifies one or
 * more groups: One group is the image key (something that uniquely identifies each image) One group
 * is the slice-identifier (identifies the z slice, must be positive integer) One group is the
 * channel-identifier (identifies the channel, must be positive integer)
 *
 * <p>For each image key, an image is loaded using the slice and channel-identifiers.
 *
 * <p>Integer numbers are simply loaded in ascending numerical order. So gaps are allowed, and
 * starting numbers are irrelevant.
 *
 * <p>It is more powerful than MultiFileReader, which expects only one image per folder. This class
 * allows multiple images per folder and only performs a single glob for filenames
 *
 * @author Owen Feehan
 */
public class GroupFiles extends InputManager<NamedChnlsInput> {

    // START BEANS
    @BeanField private Files fileInput;

    @BeanField @DefaultInstance private RasterReader rasterReader;

    @BeanField private FilePathParser filePathParser;

    @BeanField private boolean requireAllFilesMatch = false;

    @BeanField private ImgChnlMapCreator imgChnlMapCreator;

    @BeanField private DescriptiveNameFromFile descriptiveNameFromFile = new LastFolders(2);

    /**
     * Imposes a condition on each parsedFilePathBag which must be-fulfilled if a file is to be
     * included
     */
    @BeanField @OptionalBean private CheckParsedFilePathBag checkParsedFilePathBag;
    // END BEANS

    @Override
    public List<NamedChnlsInput> inputObjects(InputManagerParams params) throws AnchorIOException {

        GroupFilesMap map = new GroupFilesMap();

        // Iterate through each file, match against the reg-exp and populate a hash-map
        Iterator<FileInput> itrFiles = fileInput.inputObjects(params).iterator();
        while (itrFiles.hasNext()) {

            FileInput f = itrFiles.next();

            String path = f.getFile().getAbsolutePath();
            path = path.replaceAll("\\\\", "/");

            if (filePathParser.setPath(path)) {
                FileDetails fd =
                        new FileDetails(
                                Paths.get(path),
                                filePathParser.getChnlNum(),
                                filePathParser.getZSliceNum(),
                                filePathParser.getTimeIndex());
                map.add(filePathParser.getKey(), fd);
            } else {
                if (requireAllFilesMatch) {
                    throw new AnchorIOException(
                            String.format("File %s did not match parser", path));
                }
            }
        }
        return listFromMap(map, params.getLogger());
    }

    private List<NamedChnlsInput> listFromMap(GroupFilesMap map, Logger logger)
            throws AnchorIOException {

        List<File> files = new ArrayList<>();
        List<MultiFileReaderOpenedRaster> openedRasters = new ArrayList<>();

        // Process the hash-map by key
        for (String key : map.keySet()) {
            ParsedFilePathBag bag = map.get(key);
            assert (bag != null);

            // If we have a condition to check against
            if (checkParsedFilePathBag != null && !checkParsedFilePathBag.accept(bag)) {
                continue;
            }

            files.add(Paths.get(key).toFile());
            openedRasters.add(new MultiFileReaderOpenedRaster(rasterReader, bag));
        }

        return zipIntoGrouping(
                descriptiveNameFromFile.descriptiveNamesForCheckUniqueness(files, logger),
                openedRasters);
    }

    private List<NamedChnlsInput> zipIntoGrouping(
            List<DescriptiveFile> df, List<MultiFileReaderOpenedRaster> or) {

        Iterator<DescriptiveFile> it1 = df.iterator();
        Iterator<MultiFileReaderOpenedRaster> it2 = or.iterator();

        List<NamedChnlsInput> result = new ArrayList<>();
        while (it1.hasNext() && it2.hasNext()) {
            DescriptiveFile d = it1.next();
            result.add(new GroupingInput(d.getFile().toPath(), it2.next(), imgChnlMapCreator));
        }
        return result;
    }

    public FilePathParser getFilePathParser() {
        return filePathParser;
    }

    public void setFilePathParser(FilePathParser filePathParser) {
        this.filePathParser = filePathParser;
    }

    public boolean isRequireAllFilesMatch() {
        return requireAllFilesMatch;
    }

    public void setRequireAllFilesMatch(boolean requireAllFilesMatch) {
        this.requireAllFilesMatch = requireAllFilesMatch;
    }

    public RasterReader getRasterReader() {
        return rasterReader;
    }

    public void setRasterReader(RasterReader rasterReader) {
        this.rasterReader = rasterReader;
    }

    public Files getFileInput() {
        return fileInput;
    }

    public void setFileInput(Files fileInput) {
        this.fileInput = fileInput;
    }

    public ImgChnlMapCreator getImgChnlMapCreator() {
        return imgChnlMapCreator;
    }

    public void setImgChnlMapCreator(ImgChnlMapCreator imgChnlMapCreator) {
        this.imgChnlMapCreator = imgChnlMapCreator;
    }

    public DescriptiveNameFromFile getDescriptiveNameFromFile() {
        return descriptiveNameFromFile;
    }

    public void setDescriptiveNameFromFile(DescriptiveNameFromFile descriptiveNameFromFile) {
        this.descriptiveNameFromFile = descriptiveNameFromFile;
    }

    public CheckParsedFilePathBag getCheckParsedFilePathBag() {
        return checkParsedFilePathBag;
    }

    public void setCheckParsedFilePathBag(CheckParsedFilePathBag checkParsedFilePathBag) {
        this.checkParsedFilePathBag = checkParsedFilePathBag;
    }
}
