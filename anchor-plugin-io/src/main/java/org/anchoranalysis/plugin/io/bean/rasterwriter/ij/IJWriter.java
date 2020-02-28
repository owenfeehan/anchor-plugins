package org.anchoranalysis.plugin.io.bean.rasterwriter.ij;

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

import java.nio.file.Path;

import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.io.RasterIOException;
import org.anchoranalysis.image.io.bean.rasterwriter.RasterWriter;
import org.anchoranalysis.image.stack.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ij.ImagePlus;
import ij.io.FileSaver;



//
//  Note the difference between ImageStack (and ImageJ class)
//   and ImgStack (one of our classes)
//
public abstract class IJWriter extends RasterWriter {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(IJWriter.class);

	@Override
	public void writeStackByte( Stack stack, Path filePath, boolean makeRGB ) throws RasterIOException {
		writeStackTimeCheck( stack, filePath, makeRGB );
	}
	

	@Override
	public void writeStackShort(Stack stack, Path filePath, boolean makeRGB) throws RasterIOException {
		writeStackTimeCheck( stack, filePath, false );
	}
	
	protected abstract boolean writeRaster( FileSaver fs, String outPath, boolean asStack ) throws RasterIOException;
	
	private void writeStackTimeCheck(
		Stack stack,
		Path filePath,
		boolean makeRGB			
	) throws RasterIOException {

		if (!(stack.getNumChnl()==1 || stack.getNumChnl()==3)) {
			throw new RasterIOException("Stack must have 1 or 3 channels");
		}
		
		if (makeRGB && (stack.getNumChnl()!=3)) {
			throw new RasterIOException("To make an RGB image, the stack must have exactly 3 channels");
		}
		
		writeStackTime( stack, filePath, makeRGB );
	}
	
	protected void writeStackTime(
		Stack stack,
		Path filePath,
		boolean makeRGB
	) throws RasterIOException {
		
		try {
			
			log.debug( String.format("Writing image %s", filePath) );
			
			ImageDim sd = stack.getChnl(0).getDimensions();
			
			ImagePlus imp = IJWrap.createImagePlus(stack, makeRGB );
			
			writeImagePlus(
				imp,
				filePath,
				(stack.getChnl(0).getDimensions().getZ() > 1)
			);
			
			imp.close();
			
			assert( imp.getNSlices()==sd.getZ() );
			
			log.debug( String.format("Finished writing image %s", filePath) );
			
		} catch (CreateException e) {
			throw new RasterIOException(e);
		}
	}
	
	private void writeImagePlus( ImagePlus imp, Path filePath, boolean asStack ) throws RasterIOException {
		
		FileSaver fs = new FileSaver(imp);
		if (!writeRaster(fs, filePath.toString(), asStack)) {
			throw new RasterIOException(
				String.format("An error occured in IJ writing file '%s'",filePath)
			);
		}
	}
}
