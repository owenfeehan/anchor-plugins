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
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.filepath.prefixer.PathWithDescription;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;

@NoArgsConstructor
public class FilePathCounter extends FilePathPrefixerAvoidResolve {

    // TODO this counter should be initialized in a proper way, and not using a bean-wide variable
    private int cnt = 0;

    // START BEAN PROPERTIES
    @BeanField @Getter @Setter private int numLeadingZeros = 4;
    // END BEAN PROPERTIES

    public FilePathCounter(String outPathPrefix) {
        super(outPathPrefix);
    }

    @Override
    protected FilePathPrefix outFilePrefixFromPath(PathWithDescription input, Path root) {
        Path combinedDir = root.resolve(identifier(cnt++));
        return new FilePathPrefix(combinedDir);
    }

    private String identifier(int index) {
        String formatSpecifier = "%0" + numLeadingZeros + "d";
        return String.format(formatSpecifier, index);
    }
}
