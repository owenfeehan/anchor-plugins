package org.anchoranalysis.plugin.io.multifile.buffer;

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

import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.stack.Stack;
import org.anchoranalysis.image.stack.TimeSequence;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;
import org.anchoranalysis.plugin.io.multifile.SizeExtnts;

public class MultiBufferSized {

	private ImageDim dim;
	private int sizeT;
	private MultiBuffer buffers;
	
	public MultiBufferSized( Stack stack, SizeExtnts size ) {
		buffers = new MultiBuffer( stack, size );
		dim = new ImageDim(
			size.toExtnt(),
			stack.getDimensions().getRes()
		);
		sizeT = size.getRangeT().getSize();
	}
	
	public void populateFrom( Stack stackForFile, Integer chnlNum, Integer sliceNum, Integer timeIndex ) {

		// If timeIndex is unspecified, we assume not
		if (timeIndex==null) {
			timeIndex = 0;
		}
		
		// If we specify a channel, then we only care about slices
		if( chnlNum!=null) {
			buffers.populateWithSpecifiedChnl(stackForFile, chnlNum, sliceNum, timeIndex);
		} else {
			
			if( sliceNum!=null) {
				// No specific Channel Number, but specific Slice Number
				buffers.populateWithSpecifiedSlice(stackForFile, sliceNum, timeIndex);
				
			} else {
				buffers.populateNoSpecifics(stackForFile, timeIndex);
			}
		}
	}

	public TimeSequence createSequence(VoxelDataType dataType) {
		TimeSequence tsOut = new TimeSequence();
		for( int t=0; t<sizeT; t++) {
			tsOut.add(
				buffers.createStackForIndex(t, dim, dataType)
			);
		}
		return tsOut;
	}
}
