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


import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;
import org.anchoranalysis.image.voxel.buffer.VoxelBuffer;
import org.anchoranalysis.image.voxel.datatype.CombineTypes;
import org.anchoranalysis.image.voxel.datatype.VoxelDataType;


public class ChnlProviderIfPixelZero extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private ChnlProvider chnlProviderIfPixelZero;
	// END BEAN PROPERTIES
	
	private static void processVoxelBox( VoxelBoxWrapper vbOut, VoxelBoxWrapper vbIn, VoxelBoxWrapper vbIfZero, double multFactorIfNonZero ) {

		for (int z=0; z<vbOut.any().extnt().getZ(); z++) {
			
			VoxelBuffer<?> in1 = vbIn.any().getPixelsForPlane(z);
			VoxelBuffer<?> in2 = vbIfZero.any().getPixelsForPlane(z);
			VoxelBuffer<?> out = vbOut.any().getPixelsForPlane(z);
			
			int totalPixels = vbIn.any().extnt().getVolumeXY();
			for (int offset=0; offset<totalPixels; offset++) {
				
				int b1 = in1.getInt(offset);
								
				if (b1!=0) {
					out.putInt(offset, (int)( b1*multFactorIfNonZero) );
				} else {
					int b2 = in2.getInt(offset);
					out.putInt(offset, b2);
				}
			}
//		
//			assert( !out.hasRemaining() );
		}
	}
	
	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnl = chnlProvider.create();
		Chnl chnlIfPixelZero = chnlProviderIfPixelZero.create();
		
		VoxelDataType combinedType = CombineTypes.combineTypes(chnl.getVoxelDataType(), chnlIfPixelZero.getVoxelDataType());

		double multFact = (double) combinedType.maxValue() / chnl.getVoxelDataType().maxValue();
		return merge( chnl, chnlIfPixelZero, combinedType, multFact );
		
	}
	
	public static Chnl merge( Chnl chnl, Chnl chnlIfPixelZero, VoxelDataType combinedType, double multFactorIfNonZero ) throws CreateException {
		
		if (!chnl.getDimensions().equals(chnlIfPixelZero.getDimensions())) {
			throw new CreateException("Dimensions of channels do not match");
		}
		
		Chnl chnlOut = ChnlFactory.instance().createEmptyInitialised( new ImageDim(chnl.getDimensions()), combinedType );
		
		// We know these are all the same types from the logic above, so we can safetly cast
		processVoxelBox( chnlOut.getVoxelBox(), chnl.getVoxelBox(), chnlIfPixelZero.getVoxelBox(), multFactorIfNonZero );
		
		return chnlOut;
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}
	
	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public ChnlProvider getChnlProviderIfPixelZero() {
		return chnlProviderIfPixelZero;
	}

	public void setChnlProviderIfPixelZero(ChnlProvider chnlProviderIfPixelZero) {
		this.chnlProviderIfPixelZero = chnlProviderIfPixelZero;
	}
}
