package org.anchoranalysis.plugin.io.bean.provider.file;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.io.bean.input.InputManagerParams;
import org.anchoranalysis.io.bean.provider.file.FileProviderWithDirectory;
import org.anchoranalysis.io.error.AnchorIOException;
import org.anchoranalysis.io.params.InputContextParams;

public class SampleWithDirectory extends FileProviderWithDirectory {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private FileProviderWithDirectory fileProvider;
	
	@BeanField
	private int sampleEvery; 
	// END BEAN PROPERTIES

	@Override
	public Path getDirectoryAsPath(InputContextParams inputContext) {
		return fileProvider.getDirectoryAsPath(inputContext);
	}

	@Override
	public Collection<File> matchingFilesForDirectory(
		Path directory,
		InputManagerParams params
	) throws AnchorIOException {

		List<File> listSampled = new ArrayList<File>();
		
		Collection<File> list = fileProvider.matchingFiles(params);
		
		int cnt = -1;	// So the first item becomes 0
		for( File f : list ) {
			cnt++;
			if (cnt==sampleEvery) {
				listSampled.add(f);
				cnt=0;
			}
		}
				
		return listSampled;
	}

	public int getSampleEvery() {
		return sampleEvery;
	}

	public void setSampleEvery(int sampleEvery) {
		this.sampleEvery = sampleEvery;
	}

	public FileProviderWithDirectory getFileProvider() {
		return fileProvider;
	}

	public void setFileProvider(FileProviderWithDirectory fileProvider) {
		this.fileProvider = fileProvider;
	}
}
