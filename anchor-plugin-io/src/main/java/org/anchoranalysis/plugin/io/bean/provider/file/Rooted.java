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

package org.anchoranalysis.plugin.io.bean.provider.file;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.exception.BeanDuplicateException;
import org.anchoranalysis.core.system.path.PathDifferenceException;
import org.anchoranalysis.io.input.bean.InputManagerParams;
import org.anchoranalysis.io.input.bean.files.FilesProvider;
import org.anchoranalysis.io.input.bean.files.FilesProviderWithDirectory;
import org.anchoranalysis.io.input.files.FilesProviderException;
import org.anchoranalysis.plugin.io.input.path.RootedFilePathUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a set of files, with a different root depending on the operating-system and
 * conditions.
 *
 * <p>The following determines which root is used, and prepended to a copy of the FileSet:
 *
 * <p>If the operating system is windows:
 *
 * <ul>
 *   <li>If the fileSet directory exists in localWindowsRootPath we use that
 *   <li>Otherwise, TODO
 * </ul>
 *
 * @author Owen Feehan
 */
public class Rooted extends FilesProvider {

    // START BEAN PARAMETERS
    @BeanField @Getter @Setter private FilesProviderWithDirectory files;

    // The name of the root-path to associate with this fileset
    @BeanField @Getter @Setter private String rootName;

    // If true, we will disable debug-mode for this current bean, if debug-mode it's set. Otherwise,
    // there is no impact.
    @BeanField @Getter @Setter private boolean disableDebugMode = false;
    // END BEAN PARAMETERS

    private static Log log = LogFactory.getLog(Rooted.class);

    @Override
    public Collection<File> create(InputManagerParams params) throws FilesProviderException {

        try {
            log.debug(
                    String.format(
                            "matchingFiles() old directory '%s'%n",
                            files.getDirectoryAsPath(params.getInputContext())));

            Path directoryOriginal = files.getDirectoryAsPath(params.getInputContext());

            Path directoryNew =
                    RootedFilePathUtilities.deriveRootedPath(
                            directoryOriginal,
                            rootName,
                            params.isDebugModeActivated(),
                            disableDebugMode);

            boolean directoryNewExists = directoryNew.toFile().exists();

            // As a special behaviour, if the debug folder doesn't exist
            // we try and again with the non-debug folder
            if (params.isDebugModeActivated() && !directoryNewExists) {
                directoryNew =
                        RootedFilePathUtilities.deriveRootedPath(
                                directoryOriginal, rootName, false, disableDebugMode);
                directoryNewExists = directoryNew.toFile().exists();
            }

            if (!directoryNewExists) {
                throw new FilesProviderException(
                        String.format("Path %s' does not exist", directoryNew));
            }

            log.debug(String.format("Setting new directory '%s'%n", directoryNew));

            return files.matchingFilesForDirectory(directoryNew, params);

        } catch (BeanDuplicateException | PathDifferenceException e) {
            throw new FilesProviderException(e);
        }
    }
}
