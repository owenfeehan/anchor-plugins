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


import java.nio.FloatBuffer;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.chnl.factory.ChnlFactory;
import org.anchoranalysis.image.convert.ImgLib2Wrap;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverter;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterToUnsignedByte;
import org.anchoranalysis.image.stack.region.chnlconverter.ChnlConverterToUnsignedShort;
import org.anchoranalysis.image.stack.region.chnlconverter.ConversionPolicy;
import org.anchoranalysis.image.voxel.box.VoxelBox;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeFloat;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedShort;

import net.imglib2.img.NativeImg;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;


/**
 * Extracts the gradient
 * 
 * @author Owen Feehan
 *
 */
public class ChnlProviderGradientSingleDimension extends ChnlProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3978226156945187112L;
	
	// START BEAN
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField
	private double scaleFactor = 1.0;
	
	@BeanField
	private boolean outputShort=false;	// If true, outputs a short. Otherwise a byte
	
	@BeanField
	private int axis = 0;	// 0=X, 1=Y, 2=Z
	
	/*
	 * Added to all gradients (so we can store negative gradients) 
	 */
	@BeanField
	private int addSum = 0;
	// END BEAN

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}
	
	
	private boolean[] createDimensionArr( int dimNumber ) throws CreateException {
		switch(dimNumber) {
		case 0:
			return new boolean[] { true, false, false };
		case 1:
			return new boolean[] { false, true, false };
		case 2:
			return new boolean[] { false, false, true };
		default:
			throw new CreateException("Axis must be 0 (x-axis) or 1 (y-axis) or 2 (z-axis)");
		}
	}
	
	
	@Override
	public Chnl create() throws CreateException {
		
		Chnl chnlIn = chnlProvider.create();
		
		Chnl chnlIntermediate = ChnlFactory.instance().createEmptyInitialised( chnlIn.getDimensions(), VoxelDataTypeFloat.instance );
		VoxelBox<FloatBuffer> vb = chnlIntermediate.getVoxelBox().asFloat();

		boolean[] doDimension = createDimensionArr(axis);
		
		NativeImg<FloatType,FloatArray> natOut = ImgLib2Wrap.wrapFloat(vb, true);
		
		if (chnlIn.getVoxelDataType().equals(VoxelDataTypeUnsignedByte.instance)) {
			NativeImg<UnsignedByteType,ByteArray> natIn = ImgLib2Wrap.wrapByte(chnlIn.getVoxelBox().asByte(), true);
			ChnlProviderGradientFilter.process(natIn,natOut, doDimension, (float) scaleFactor, false, false, addSum);
		} else if (chnlIn.getVoxelDataType().equals(VoxelDataTypeUnsignedShort.instance)) {
			NativeImg<UnsignedShortType,ShortArray> natIn = ImgLib2Wrap.wrapShort(chnlIn.getVoxelBox().asShort(), true );
			ChnlProviderGradientFilter.process(natIn,natOut, doDimension, (float) scaleFactor, false, false, addSum);
		} else {
			throw new CreateException("Input type must be unsigned byte or short");
		}
		
		// convert to our output from the float
		ChnlConverter<?> converter = outputShort ? new ChnlConverterToUnsignedShort() : new ChnlConverterToUnsignedByte();
		return converter.convert(chnlIntermediate, ConversionPolicy.CHANGE_EXISTING_CHANNEL );
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public boolean isOutputShort() {
		return outputShort;
	}

	public void setOutputShort(boolean outputShort) {
		this.outputShort = outputShort;
	}

	public int getAxis() {
		return axis;
	}

	public void setAxis(int axis) {
		this.axis = axis;
	}

	public int getAddSum() {
		return addSum;
	}

	public void setAddSum(int addSum) {
		this.addSum = addSum;
	}

}
