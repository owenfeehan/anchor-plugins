package org.anchoranalysis.plugin.io.bean.summarizer.path;

/*-
 * #%L
 * anchor-plugin-io
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.plugin.io.bean.summarizer.Summarizer;
import org.apache.commons.io.IOCase;

import com.owenfeehan.pathpatternfinder.PathPatternFinder;
import com.owenfeehan.pathpatternfinder.Pattern;


/**
 * Converts a list of file-paths into a form that tries to find a pattern in the
 *   naming style using the path-pattern-finder library
 */
public class FilePathPattern extends Summarizer<Path> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN PROPERTIES
	/** Iff TRUE, any hidden-path is not considered, and simply ignored */
	private boolean ignoreHidden = true;
	
	/** if TRUE, case is ignored in the pattern matching. Otherwise the system-default is used
	 *  i.e. Windows ingores case, Linux doesn't
	 */
	private boolean ignoreCase = false;
	// END BEAN PROPERTIES
	
	private List<Path> paths = new ArrayList<>();
	
	@Override
	public synchronized void add(Path element) throws OperationFailedException {
		try {
			// Always accept the path if it doesn't exist on the file-system
			if (!element.toFile().exists() || !ignoreHidden || !Files.isHidden(element) ) {
				paths.add(element);
			}
		} catch (IOException e) {
			throw new OperationFailedException(e);
		}
	}

	@Override
	public synchronized String describe() throws OperationFailedException {
		
		if (paths.isEmpty()) {
			throw new OperationFailedException("There are no paths to summarize");
		}
		
		if (paths.size()==1) {
			// There is no pattern possible, so just return the path as is
			return paths.get(0).toString();
		}
		
		Pattern pattern = PathPatternFinder.findPatternPath(paths, selectIOCase() );
		return pattern.describeDetailed();
	}
		
	private IOCase selectIOCase() {
		return ignoreCase ? IOCase.INSENSITIVE : IOCase.SYSTEM;
	}

	public boolean isIgnoreHidden() {
		return ignoreHidden;
	}

	public void setIgnoreHidden(boolean ignoreHidden) {
		this.ignoreHidden = ignoreHidden;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}

}
