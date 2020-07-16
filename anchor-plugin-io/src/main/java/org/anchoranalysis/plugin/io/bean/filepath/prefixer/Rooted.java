/* (C)2020 */
package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

import java.nio.file.Path;
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
    @BeanField private FilePathPrefixerAvoidResolve filePathPrefixer;

    // The name of the RootPath to associate with this fileset
    @BeanField private String rootName;
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
                    RootPathMap.instance().split(input.getPath(), rootName, debugMode).getPath();
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

    public FilePathPrefixerAvoidResolve getFilePathPrefixer() {
        return filePathPrefixer;
    }

    public void setFilePathPrefixer(FilePathPrefixerAvoidResolve filePathPrefixer) {
        this.filePathPrefixer = filePathPrefixer;
    }

    public String getRootName() {
        return rootName;
    }

    public void setRootName(String rootName) {
        this.rootName = rootName;
    }
}
