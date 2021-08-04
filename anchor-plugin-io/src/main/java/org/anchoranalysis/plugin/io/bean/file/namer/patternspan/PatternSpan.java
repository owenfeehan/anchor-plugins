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

package org.anchoranalysis.plugin.io.bean.file.namer.patternspan;

import com.owenfeehan.pathpatternfinder.PathPatternFinder;
import com.owenfeehan.pathpatternfinder.Pattern;
import com.owenfeehan.pathpatternfinder.patternelements.PatternElement;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.exception.friendly.AnchorImpossibleSituationException;
import org.anchoranalysis.core.functional.FunctionalList;
import org.anchoranalysis.core.system.path.ExtensionUtilities;
import org.anchoranalysis.io.input.bean.namer.FileNamer;
import org.anchoranalysis.io.input.file.FileNamerContext;
import org.anchoranalysis.io.input.file.NamedFile;
import org.apache.commons.io.IOCase;

/**
 * Finds a pattern in the descriptive name, and uses the region from the first variable to the
 * last-variable as the descriptive-name.
 *
 * <p>In the special case that a single file is passed, its filename is extracted without any
 * extension.
 *
 * @author Owen Feehan
 */
public class PatternSpan extends FileNamer {

    // START BEAN PROPERTIES
    /** Iff true, a case sensitive search is used to match patterns. */
    @BeanField @Getter @Setter private boolean caseSensitive = false;
    // END BEAN PROPERTIES

    @Override
    public List<NamedFile> deriveName(Collection<File> files, FileNamerContext context) {

        // Convert to list
        List<Path> paths = convertToList(files);

        if (paths.size() <= 1) {
            // Everything's a constant, so there must only be a single file. Return the file-name.
            return extractFileNames(files);
        }

        IOCase ioCase = createCaseSensitivity();

        Pattern pattern = PathPatternFinder.findPatternPaths(paths, ioCase, true);
        if (!hasAtLeastOneVariableElement(pattern)) {
            throw new AnchorImpossibleSituationException();
        }

        return extractFromPattern(pattern, files, ioCase, context);
    }

    private IOCase createCaseSensitivity() {
        return caseSensitive ? IOCase.SENSITIVE : IOCase.INSENSITIVE;
    }

    private static List<NamedFile> extractFromPattern(
            Pattern pattern, Collection<File> files, IOCase ioCase, FileNamerContext context) {
        ExtractVariableSpan extracter =
                new SelectSpanToExtract(pattern, context.getNameSubrange())
                        .selectSpanToExtract(context.getElseName());
        return ExtractVariableSpanForList.listExtract(files, extracter, ioCase);
    }

    private static boolean hasAtLeastOneVariableElement(Pattern pattern) {
        for (PatternElement element : pattern) {
            if (!element.hasConstantValue()) {
                return true;
            }
        }
        return false;
    }

    /** Extracts the file-name (without any extension). */
    private static List<NamedFile> extractFileNames(Collection<File> files) {
        return FunctionalList.mapToList(
                files,
                file -> new NamedFile(ExtensionUtilities.filenameWithoutExtension(file), file));
    }

    /** Convert {@link File} to {@link Path}. */
    private static List<Path> convertToList(Collection<File> files) {
        return FunctionalList.mapToList(files, File::toPath);
    }
}
