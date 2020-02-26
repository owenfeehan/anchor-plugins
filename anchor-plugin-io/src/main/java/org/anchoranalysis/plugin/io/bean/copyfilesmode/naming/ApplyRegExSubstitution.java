package org.anchoranalysis.plugin.io.bean.copyfilesmode.naming;

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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.error.AnchorIOException;

/**
 * A regular expression substitution (replaceAll) is applied to the relative-path
 *   
 * @author FEEHANO
 *
 */
public class ApplyRegExSubstitution extends CopyFilesNaming {

	// START BEAN PROPERTIES
	@BeanField
	private CopyFilesNaming copyFilesNaming;
	
	@BeanField
	private String regex;
	
	@BeanField
	private String replacement;
	// END BEAN PROPERTIES
	
	@Override
	public void beforeCopying(Path destDir, int totalNumFiles) {
		copyFilesNaming.beforeCopying(destDir, totalNumFiles);
	}

	@Override
	public Path destinationPathRelative(Path sourceDir, Path destDir, File file, int iter) throws AnchorIOException {
		Path path = copyFilesNaming.destinationPathRelative(sourceDir, destDir, file, iter);

		if (path==null) {
			return null;
		}
		
		String pathAfterRegEx = NamingUtilities.convertToString(path).replaceAll(regex,replacement);
		
		return Paths.get(pathAfterRegEx);
	}

	@Override
	public void afterCopying(Path destDir, boolean dummyMode) throws AnchorIOException {
		copyFilesNaming.afterCopying(destDir, dummyMode);
	}

	public CopyFilesNaming getCopyFilesNaming() {
		return copyFilesNaming;
	}

	public void setCopyFilesNaming(CopyFilesNaming copyFilesNaming) {
		this.copyFilesNaming = copyFilesNaming;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

}
