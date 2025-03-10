/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2021 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann-La Roche
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
package org.anchoranalysis.plugin.io.bean.file.namer;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import org.anchoranalysis.core.exception.CreateException;
import org.anchoranalysis.core.system.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.bean.namer.FileNamerIndependent;

/**
 * Constructs a name by finding the relative-path between the file and the input-directory.
 *
 * <p>If no input-directory exists, a name cannot be constructed.
 *
 * <p>Any directory seperator in the he path is always expressed with forward-slashes, even if
 * backslashes are used by the operating system.
 *
 * @author Owen Feehan
 */
public class RelativeToDirectory extends FileNamerIndependent {

    @Override
    protected String deriveName(File file, Optional<Path> inputDirectory, int index)
            throws CreateException {

        if (inputDirectory.isPresent()) {
            Path inputDirectoryAbsolute = makeAbsoluteNormalized(inputDirectory.get());
            Path fileAbsolute = makeAbsoluteNormalized(file.toPath());
            return FilePathToUnixStyleConverter.toStringUnixStyle(
                    inputDirectoryAbsolute.relativize(fileAbsolute));
        } else {
            throw new CreateException(
                    "Cannot derive a name as no input-directory is defined, as is required.");
        }
    }

    private static Path makeAbsoluteNormalized(Path path) {
        return path.toAbsolutePath().normalize();
    }
}
