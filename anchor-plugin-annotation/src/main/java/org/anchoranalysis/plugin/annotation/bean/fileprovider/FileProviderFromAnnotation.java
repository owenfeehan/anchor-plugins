package org.anchoranalysis.plugin.annotation.bean.fileprovider;

/*
 * #%L
 * anchor-annotation
 * %%
 * Copyright (C) 2016 ETH Zurich, University of Zurich, Owen Feehan
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.anchoranalysis.annotation.io.bean.input.AnnotationInputManager;
import org.anchoranalysis.annotation.io.bean.strategy.AnnotatorStrategy;
import org.anchoranalysis.annotation.io.input.AnnotationWithStrategy;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.progress.ProgressReporter;
import org.anchoranalysis.image.io.input.NamedChnlsInputBase;
import org.anchoranalysis.io.bean.provider.file.FileProvider;
import org.anchoranalysis.io.deserializer.DeserializationFailedException;
import org.anchoranalysis.io.params.InputContextParams;

public class FileProviderFromAnnotation<T extends AnnotatorStrategy> extends FileProvider {
	
	// START BEAN PROPERTIES
	@BeanField
	private AnnotationInputManager<NamedChnlsInputBase,T> annotationInputManager;
	// END BEAN PROPERTIES
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Collection<File> matchingFiles(ProgressReporter progressReporter, InputContextParams inputContext)
			throws IOException {

		List<File> filesOut = new ArrayList<File>();
		
		try {
			List<AnnotationWithStrategy<T>> list = annotationInputManager.inputObjects(inputContext, progressReporter);
			for( AnnotationWithStrategy<T> inp : list ) {
								
				filesOut.addAll(
					inp.deriveAssociatedFiles()
				);
				
			}
			
		} catch (DeserializationFailedException e) {
			throw new IOException(e);
		}
		
		return filesOut;
	}
		
	public AnnotationInputManager<NamedChnlsInputBase,T> getAnnotationInputManager() {
		return annotationInputManager;
	}

	public void setAnnotationInputManager(
			AnnotationInputManager<NamedChnlsInputBase,T> annotationInputManager) {
		this.annotationInputManager = annotationInputManager;
	}

}
