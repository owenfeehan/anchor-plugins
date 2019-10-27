package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-image
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


import java.nio.ByteBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.ByteConverter;
import org.anchoranalysis.image.extent.Extent;
import org.anchoranalysis.image.histogram.Histogram;
import org.anchoranalysis.image.histogram.HistogramFactoryUtilities;
import org.anchoranalysis.image.voxel.box.VoxelBox;

public class ChnlProviderSuppressAbove extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private BinaryImgChnlProvider binaryImgChnlProviderMask;
	
	@BeanField
	private double quantile = 0.5;
	// END BEAN PROPERTIES

	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		
		BinaryChnl binaryImg = binaryImgChnlProviderMask.create();
		
		Histogram hist = HistogramFactoryUtilities.create(chnl, binaryImg );
		double intensityThrshldDbl = hist.quantile(quantile);
		
		//System.out.printf("Mean Intensity Dbl for quantile %f is %f\n", quantile, meanIntensityDbl);
		
		int meanIntensity = (int) Math.ceil(intensityThrshldDbl);
		byte meanIntensityByte = (byte) meanIntensity;

		VoxelBox<ByteBuffer> vb = chnl.getVoxelBox().asByte();
		Extent e = vb.extnt();
		
		for( int z=0; z<e.getZ(); z++) {
			
			ByteBuffer bb = vb.getPixelsForPlane(z).buffer();
			
			int offset = 0;
			for( int y=0; y<e.getY(); y++) {
				for( int x=0; x<e.getX(); x++) {
			
					int val = ByteConverter.unsignedByteToInt(bb.get(offset));
					if (val>meanIntensity) {
						bb.put(offset, meanIntensityByte );
					}
					
					offset++;
				}
			}
			
		}
			
		return chnl;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public double getQuantile() {
		return quantile;
	}

	public void setQuantile(double quantile) {
		this.quantile = quantile;
	}


	public BinaryImgChnlProvider getBinaryImgChnlProviderMask() {
		return binaryImgChnlProviderMask;
	}


	public void setBinaryImgChnlProviderMask(
			BinaryImgChnlProvider binaryImgChnlProviderMask) {
		this.binaryImgChnlProviderMask = binaryImgChnlProviderMask;
	}





}
