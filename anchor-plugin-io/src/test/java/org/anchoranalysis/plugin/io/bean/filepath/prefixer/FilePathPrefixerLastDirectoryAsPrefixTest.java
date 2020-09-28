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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.anchoranalysis.io.bean.filepath.prefixer.NamedPath;
import org.anchoranalysis.io.error.FilePathPrefixerException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;
import org.junit.Test;

public class FilePathPrefixerLastDirectoryAsPrefixTest {

    @Test
    public void test() throws FilePathPrefixerException {

        Path root = mock(Path.class);

        NamedPath path = new NamedPath(Paths.get("/a/b/c/d/e/somefile.tif"), "somefile");

        LastDirectoryAsPrefix prefixer = new LastDirectoryAsPrefix();
        prefixer.setFilePathPrefixer(createDelegate(path, root));

        FilePathPrefix out = prefixer.outFilePrefixFromPath(path, root);

        assertEquals(Paths.get("/g/h"), out.getFolderPath());
        assertEquals("i_outprefix", out.getFilenamePrefix());
    }

    private FilePathPrefixerAvoidResolve createDelegate(NamedPath path, Path root)
            throws FilePathPrefixerException {
        FilePathPrefixerAvoidResolve prefixer = mock(FilePathPrefixerAvoidResolve.class);
        when(prefixer.outFilePrefixFromPath(path, root))
                .thenReturn(new FilePathPrefix(Paths.get("/g/h/i/"), "outprefix"));
        return prefixer;
    }
}
