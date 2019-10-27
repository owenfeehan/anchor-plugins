package org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tobyte;

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

import java.nio.ByteBuffer;

import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.buffer.VoxelBufferByte;

import loci.common.DataTools;

public class ConvertToByte_From16Bit extends ConvertToByte {

	private int bytesPerPixel;
	private int sizeXY;
	private int sizeBytes;
	
	private boolean littleEndian;
	private int maxTotalBits;
	
	public ConvertToByte_From16Bit(boolean littleEndian, int maxTotalBits) {
		super();
		this.littleEndian = littleEndian;
		this.maxTotalBits = maxTotalBits;
	}		
	
	@Override
	protected void setupBefore(ImageDim sd, int numChnlsPerByteArray) {
		bytesPerPixel = 2 * numChnlsPerByteArray;
  		sizeXY = sd.getX() * sd.getY();
  		sizeBytes = sizeXY * bytesPerPixel;
	}
	
	@Override
	protected VoxelBuffer<ByteBuffer> convertSingleChnl(byte[] src, int c_rel) {
		// we assign a default that maps from 16-bit to 8-bit
		ApplyScaling applyScaling = new ApplyScaling(
			ConvertHelper.twoToPower(8-maxTotalBits),
			0
		);
		  
		byte[] crntChnlBytes = new byte[sizeXY];
		
		int indOut = 0;
		for(int indIn =0; indIn<sizeBytes; indIn+=bytesPerPixel) {
			int s = (int) DataTools.bytesToShort( src, indIn + (c_rel*2), 2, littleEndian);
			
			// Make unsigned
			if (s<0) {
				s+= 65536;
			}
			
			if (applyScaling!=null) {
				s = applyScaling.apply(s);
			}
			
			if (s>255) {
				s = 255;
			}
			if (s<0) {
				s = 0;
			}
			
			crntChnlBytes[indOut++] = (byte)( s );
		}
		return VoxelBufferByte.wrap( crntChnlBytes );
	}
}
