package org.anchoranalysis.plugin.io.bean.summarizer.image;

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

import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.input.NamedChnlsInput;

/**
 * Summarizes the size of images.
 * 
 * If there's more than one image in the series, the size of each is considered.
 * 
 **/
public class ImageSize extends SummarizerNamedChnls<WrappedImageDim> {

	@Override
	public void add( NamedChnlsInput img ) throws OperationFailedException {

		try {
			int numSeries = img.numSeries();
			for( int i=0; i<numSeries; i++ ) {
			
				ImageDim dim = img.dim(0);
				incrCount( new WrappedImageDim(dim) );
			}
			
		} catch (RasterIOException exc) {
			throw new OperationFailedException(exc);
		}
	}
	
	@Override
	protected String describeNoun() {
		return "size";
	}
}
