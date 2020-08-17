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

package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.nio.file.Path;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.bean.root.RootPathMap;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.error.FilePathPrefixerException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefixerParams;
import org.apache.log4j.Logger;

/**
 * Prepend a 'root' before the file-path-prefix obtained from a delegate
 *
 * <p>A root is a path that is mapped via a unique-name in a settings file to a directory
 *
 * @author Owen Feehan
 */
public class Rooted extends FilePathPrefixer {

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private FilePathPrefixerAvoidResolve filePathPrefixer;

    // The name of the RootPath to associate with this fileset
    @BeanField @Getter @Setter private String rootName;
    // END BEAN PROPERTIES

    private static Logger logger = Logger.getLogger(Rooted.class);

    @Override
    public FilePathPrefix outFilePrefix(
            PathWithDescription input, String expName, FilePathPrefixerParams context)
            throws FilePathPrefixerException {

        logger.debug(String.format("pathIn=%s", input));

        FilePathPrefix fpp =
                filePathPrefixer.outFilePrefixAvoidResolve(
                        removeRoot(input, context.isDebugMode()), expName);

        Path pathOut = folderPathOut(fpp.getFolderPath(), context.isDebugMode());
        fpp.setFolderPath(pathOut);

        logger.debug(String.format("prefix=%s", fpp.getFolderPath()));
        logger.debug(String.format("multiRoot+Rest()=%s", pathOut));

        return fpp;
    }

    private PathWithDescription removeRoot(PathWithDescription input, boolean debugMode)
            throws FilePathPrefixerException {
        try {
            Path pathInWithoutRoot =
                    RootPathMap.instance()
                            .split(input.getPath(), rootName, debugMode)
                            .getRemainder();
            return new PathWithDescription(pathInWithoutRoot, input.getDescriptiveName());
        } catch (AnchorIOException e) {
            throw new FilePathPrefixerException(e);
        }
    }

    @Override
    public FilePathPrefix rootFolderPrefix(String expName, FilePathPrefixerParams context)
            throws FilePathPrefixerException {
        FilePathPrefix fpp = filePathPrefixer.rootFolderPrefixAvoidResolve(expName);
        fpp.setFolderPath(folderPathOut(fpp.getFolderPath(), context.isDebugMode()));
        return fpp;
    }

    private Path folderPathOut(Path pathIn, boolean debugMode) throws FilePathPrefixerException {
        try {
            return RootPathMap.instance().findRoot(rootName, debugMode).asPath().resolve(pathIn);
        } catch (AnchorIOException e) {
            throw new FilePathPrefixerException(e);
        }
    }
}
