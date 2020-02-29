package org.anchoranalysis.plugin.io.bean.filepath.prefixer;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.filepath.prefixer.FilePathPrefix;


/**
 * The prefixer uses a combination of a out-path-prefix and the descriptive-name of inputs to create an output prefix
 * @author owen
 *
 */
public class FilePathPrefixerFromDescriptiveName extends FilePathPrefixerAvoidResolve {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected FilePathPrefix outFilePrefixFromPath(Path path, String descriptiveName, Path root)
			throws AnchorIOException {
		Path combined = root.resolve( Paths.get(descriptiveName));
		return new FilePathPrefix(combined);
	}

}
