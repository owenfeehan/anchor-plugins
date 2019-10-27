package org.anchoranalysis.plugin.io.rasterwriter.bioformats.copyconvert.tofloat;

/*
 * #%L
 * anchor-plugin-io
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


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.anchoranalysis.image.extent.ImageDim;

import com.google.common.io.LittleEndianDataInputStream;

public class ConvertToFloat_From32BitFloat extends ConvertToFloat {

	private boolean littleEndian;
	
	public ConvertToFloat_From32BitFloat( boolean littleEndian ) {
		super();
		this.littleEndian = littleEndian;
	}

	@Override
	protected float[] convertIntegerBytesToFloatArray(ImageDim sd, byte[] src, int srcOffset) throws IOException {
		
		float[] fArr = new float[sd.getX()*sd.getY()];
		int cntLoc = 0;
		
		
		ByteArrayInputStream bis = new ByteArrayInputStream(src);
		
		if (littleEndian) {
			
			try (LittleEndianDataInputStream dis = new LittleEndianDataInputStream (bis)) {
				for (int y=0; y<sd.getY(); y++) {
					  for (int x=0; x<sd.getX(); x++) {  
						  fArr[cntLoc++] = dis.readFloat();
					  }
				}
				return fArr;
			}
			
		} else {

			try (DataInputStream dis = new DataInputStream(bis) ) {
				for (int y=0; y<sd.getY(); y++) {
					  for (int x=0; x<sd.getX(); x++) {
						  
						  float f = dis.readFloat();
						  fArr[cntLoc++] = f;
					  }
				}
				return fArr;
			}
		}
	}

	@Override
	protected int bytesPerPixel() {
		return 4;
	}

}
