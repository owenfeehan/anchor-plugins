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
import java.util.Collection;
import java.util.Iterator;
import lombok.Getter;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.shared.regex.RegEx;
import org.anchoranalysis.core.path.FilePathToUnixStyleConverter;
import org.anchoranalysis.io.input.bean.files.FilesProviderUnary;

// Removes one or more files if they match a regex
public class RemoveMatching extends FilesProviderUnary {

    // START BEAN PROPERTIES
    /** If a path matches a regular expression it is removed. */
    @BeanField @Getter @Setter private RegEx regEx;
    // END BEAN PROPERTIES

    @Override
    protected Collection<File> transform(Collection<File> source, boolean debugMode) {

        // Loop through each file and see if it's in our has map
        Iterator<File> itr = source.iterator();
        while (itr.hasNext()) {
            File f = itr.next();

            String normalizedPath = FilePathToUnixStyleConverter.toStringUnixStyle(f.toPath());
            if (regEx.hasMatch(normalizedPath)) {
                itr.remove();
            }
        }
        return source;
    }
}
