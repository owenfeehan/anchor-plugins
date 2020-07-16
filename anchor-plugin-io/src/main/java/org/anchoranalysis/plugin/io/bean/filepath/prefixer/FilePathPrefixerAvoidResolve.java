/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.bean.annotation.AllowEmpty;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.prefixer.FilePathPrefixer;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.error.AnchorIOException;
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
public abstract class FilePathPrefixerAvoidResolve extends FilePathPrefixer {

    // START BEAN PROPERTIES
    /**
     * A directory in which to output the experiment-directory and files
     *
     * <p>If empty, first the bean will try to use any output-dir set in the input context if it
     * exists, or otherwise use the system temp dir
     */
    @BeanField @AllowEmpty private String outPathPrefix = "";
    // END BEAN PROPERTIES

    // Caches the calculation
    private Path resolvedRoot = null;

    public FilePathPrefixerAvoidResolve() {}

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
     * @param pathIn an input-path to match against
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
     * @param path path to calculate prefix from
     * @param descriptiveName descriptive-name of input
     * @param root root of prefix
     * @return folder/filename for prefixing
     * @throws AnchorIOException
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

    public String getOutPathPrefix() {
        return outPathPrefix;
    }

    public void setOutPathPrefix(String outPathPrefix) {
        this.outPathPrefix = outPathPrefix;
    }
}
