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

package org.anchoranalysis.plugin.io.bean.file.copy.naming;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.anchoranalysis.core.system.path.PathDifferenceException;
import org.anchoranalysis.experiment.task.NoSharedState;
import org.anchoranalysis.io.input.file.FileWithDirectoryInput;
import org.anchoranalysis.io.output.error.OutputWriteFailedException;
import org.anchoranalysis.io.output.path.prefixer.DirectoryWithPrefix;
import org.anchoranalysis.plugin.io.file.copy.PathOperations;
import org.anchoranalysis.plugin.io.input.path.CopyContext;

/**
 * Copies files to maintain the same relative-path from the destination file to the
 * destination-directory, as existed from the source file to the source-directory.
 *
 * @author Owen Feehan
 */
public class PreserveName extends CopyFilesNamingWithoutSharedState {

    @Override
    public NoSharedState beforeCopying(
            Path destinationDirectory, List<FileWithDirectoryInput> inputs) {
        // NOTHING TO DO
        return NoSharedState.INSTANCE;
    }

    @Override
    public Optional<Path> destinationPathRelative(
            File file,
            DirectoryWithPrefix outputTarget,
            int iter,
            CopyContext<NoSharedState> context)
            throws OutputWriteFailedException {
        try {
            return Optional.of(
                    PathOperations.filePathDifference(context.getSourceDirectory(), file.toPath()));
        } catch (PathDifferenceException e) {
            throw new OutputWriteFailedException(e);
        }
    }
}
