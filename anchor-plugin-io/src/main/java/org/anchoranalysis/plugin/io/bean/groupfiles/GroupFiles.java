/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

package org.anchoranalysis.plugin.io.bean.groupfiles;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.log.Logger;
import org.anchoranalysis.image.io.bean.channel.ChannelMap;
import org.anchoranalysis.image.io.bean.stack.reader.InputManagerWithStackReader;
import org.anchoranalysis.image.io.channel.input.NamedChannelsInput;
import org.anchoranalysis.io.input.InputReadFailedException;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.descriptivename.FileNamer;
import org.anchoranalysis.io.input.files.FileInput;
import org.anchoranalysis.io.input.files.NamedFile;
import org.anchoranalysis.plugin.io.bean.descriptivename.LastDirectories;
import org.anchoranalysis.plugin.io.bean.groupfiles.check.CheckParsedFilePathBag;
import org.anchoranalysis.plugin.io.bean.groupfiles.parser.FilePathParser;
import org.anchoranalysis.plugin.io.bean.input.files.NamedFiles;
import org.anchoranalysis.plugin.io.bean.stack.reader.MultiFileReader;
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
 * <p>It is more powerful than {@link MultiFileReader}, which expects only one image per folder.
 * This class allows multiple images per folder and only performs a single glob for filenames.
 *
 * @author Owen Feehan
 */
public class GroupFiles extends InputManagerWithStackReader<NamedChannelsInput> {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private NamedFiles fileInput;

    @BeanField @Getter @Setter private FilePathParser pathParser;

    @BeanField @Getter @Setter private boolean requireAllFilesMatch = false;

    @BeanField @Getter @Setter private ChannelMap imgChannelMapCreator;

    @BeanField @Getter @Setter private FileNamer namer = new LastDirectories(2);

    /**
     * Imposes a condition on each parsed-file-path-bag which must be-fulfilled if a file is to be
     * included.
     */
    @BeanField @OptionalBean @Getter @Setter private CheckParsedFilePathBag checkParsedFilePathBag;
    // END BEAN PROPERTIES

    @Override
    public List<NamedChannelsInput> inputs(InputManagerParams params)
            throws InputReadFailedException {

        GroupFilesMap map = new GroupFilesMap();

        // Iterate through each file, match against the reg-exp and populate a hash-map
        Iterator<FileInput> iterator = fileInput.inputs(params).iterator();
        while (iterator.hasNext()) {

            FileInput input = iterator.next();

            Optional<FileDetails> details = pathParser.parsePath(input.getFile().toPath());
            if (details.isPresent()) {
                map.add(pathParser.getKey(), details.get());
            } else {
                if (requireAllFilesMatch) {
                    throw new InputReadFailedException(
                            String.format(
                                    "File %s did not match parser", input.getFile().toPath()));
                }
            }
        }
        return listFromMap(map, params.getLogger());
    }

    private List<NamedChannelsInput> listFromMap(GroupFilesMap map, Logger logger)
            throws InputReadFailedException {

        List<File> files = new ArrayList<>();
        List<MultiFileReaderOpenedRaster> openedFiles = new ArrayList<>();

        // Process the hash-map by key
        for (String key : map.keySet()) {
            ParsedFilePathBag bag = map.get(key);

            // If we have a condition to check against
            if (checkParsedFilePathBag == null || checkParsedFilePathBag.accept(bag)) {
                files.add(Paths.get(key).toFile());
                openedFiles.add(new MultiFileReaderOpenedRaster(getStackReader(), bag));
            }
        }

        return zipIntoGrouping(namer.deriveNameUnique(files, logger), openedFiles);
    }

    private List<NamedChannelsInput> zipIntoGrouping(
            List<NamedFile> files, List<MultiFileReaderOpenedRaster> openedFiles) {

        Iterator<NamedFile> iterator1 = files.iterator();
        Iterator<MultiFileReaderOpenedRaster> iterator2 = openedFiles.iterator();

        List<NamedChannelsInput> result = new ArrayList<>();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            result.add(
                    new GroupingInput(
                            iterator1.next().getFile().toPath(),
                            iterator2.next(),
                            imgChannelMapCreator));
        }
        return result;
    }
}
