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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.FilePathPrefixerException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefixerParams;

/**
 * A file-path-resolver that adds additional methods that perform the same function but output a
 * relative-path rather than an absolute path after fully resolving paths
 *
 * <p>This is useful for combining with the MultiRootedFilePathPrefixer. This results in
 * relative-paths keeping the same root, as when passed in
 *
 * @author Owen Feehan
 */
@NoArgsConstructor
public abstract class FilePathPrefixerAvoidResolve extends FilePathPrefixer {

    // START BEAN PROPERTIES
    /**
     * A directory in which to output the experiment-directory and files
     *
     * <p>If empty, first the bean will try to use any output-dir set in the input context if it
     * exists, or otherwise use the system temp dir
     */
    @BeanField @AllowEmpty @Getter @Setter private String outPathPrefix = "";
    // END BEAN PROPERTIES

    // Caches the calculation
    private Path resolvedRoot = null;

    public FilePathPrefixerAvoidResolve(String outPathPrefix) {
        this.outPathPrefix = outPathPrefix;
    }

    @Override
    public FilePathPrefix rootFolderPrefix(String expName, FilePathPrefixerParams context)
            throws FilePathPrefixerException {
        return new FilePathPrefix(resolveExperimentAbsoluteRootOut(expName, context));
    }

    @Override
    public FilePathPrefix outFilePrefix(
            PathWithDescription input, String expName, FilePathPrefixerParams context)
            throws FilePathPrefixerException {

        Path root = resolveExperimentAbsoluteRootOut(expName, context);
        return outFilePrefixFromPath(input, root);
    }

    /**
     * Provides a prefix that becomes the root-folder. It avoids resolving relative-paths.
     *
     * <p>This is an alternative method to rootFolderPrefix that avoids resolving the out-path
     * prefix against the file system
     *
     * @param experimentIdentifier an identifier for the experiment
     * @return a prefixer
     */
    public FilePathPrefix rootFolderPrefixAvoidResolve(String experimentIdentifier) {
        String folder = getOutPathPrefix() + File.separator + experimentIdentifier + File.separator;
        return new FilePathPrefix(Paths.get(folder));
    }

    /**
     * Provides a prefix which can be prepended to all output files. It avoids resolving
     * relative-paths.
     *
     * @param input an input-path to match against
     * @param experimentIdentifier an identifier for the experiment
     * @return a prefixer
     * @throws FilePathPrefixerException
     */
    public FilePathPrefix outFilePrefixAvoidResolve(
            PathWithDescription input, String experimentIdentifier)
            throws FilePathPrefixerException {
        return outFilePrefixFromPath(
                input, rootFolderPrefixAvoidResolve(experimentIdentifier).getFolderPath());
    }

    /**
     * Determines the out-file prefix from a path
     *
     * @param input path to calculate prefix from with associated descriptive-name
     * @param root root of prefix
     * @return folder/filename for prefixing
     */
    protected abstract FilePathPrefix outFilePrefixFromPath(PathWithDescription input, Path root)
            throws FilePathPrefixerException;

    /** The root of the experiment for outputting files */
    private Path resolveExperimentAbsoluteRootOut(String expName, FilePathPrefixerParams context) {

        if (resolvedRoot == null) {
            resolvedRoot = selectResolvedPath(context).resolve(expName);
        }
        return resolvedRoot;
    }

    private Path selectResolvedPath(FilePathPrefixerParams context) {

        if (outPathPrefix.isEmpty()) {
            // If there's an outPathPrefix specified, then use it, otherwise a temporary directory
            return context.getOutputDirectory().orElseGet(FilePathPrefixerAvoidResolve::tempDir);
        }

        return resolvePath(outPathPrefix);
    }

    private static Path tempDir() {
        String tempDir = System.getProperty("java.io.tmpdir");
        return Paths.get(tempDir);
    }
}
