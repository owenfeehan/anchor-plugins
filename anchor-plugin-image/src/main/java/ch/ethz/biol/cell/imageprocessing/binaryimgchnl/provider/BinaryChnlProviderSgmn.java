package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

/*-
 * #%L
 * anchor-plugin-image
 * %%
 * Copyright (C) 2010 - 2020 Owen Feehan
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
import java.util.Optional;

import org.anchoranalysis.bean.OptionalFactory;
import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.OptionalBean;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.BinaryChnlProvider;
import org.anchoranalysis.image.bean.provider.HistogramProvider;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmn;
import org.anchoranalysis.image.bean.sgmn.binary.BinarySgmnParameters;
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.binary.voxel.BinaryVoxelBox;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.channel.factory.ChannelFactory;
import org.anchoranalysis.image.extent.ImageDim;
import org.anchoranalysis.image.objectmask.ObjectMask;
import org.anchoranalysis.image.sgmn.SgmnFailedException;
import org.anchoranalysis.image.voxel.datatype.VoxelDataTypeUnsignedByte;

public class BinaryChnlProviderSgmn extends BinaryChnlProviderChnlSource {

	// START BEAN PROPERTIES
	@BeanField
	private BinarySgmn sgmn;
	
	@BeanField @OptionalBean 
	private HistogramProvider histogramProvider;
	
	@BeanField @OptionalBean
	private BinaryChnlProvider mask;
	// END BEAN PROPERTIES
	
	@Override
	protected BinaryChnl createFromSource(Channel chnlSource) throws CreateException {
		return new BinaryChnl(
				sgmnResult(chnlSource),
				chnlSource.getDimensions().getRes(),
				ChannelFactory.instance().get(VoxelDataTypeUnsignedByte.instance)
			);
	}
	
	private BinaryVoxelBox<ByteBuffer> sgmnResult(Channel chnl) throws CreateException {
		Optional<ObjectMask> omMask = mask(chnl.getDimensions());
		
		BinarySgmnParameters params = createParams(chnl.getDimensions()); 

		try {
			return sgmn.sgmn(chnl.getVoxelBox(), params, omMask);
		
		} catch (SgmnFailedException e) {
			throw new CreateException(e);
		}
	}

	private BinarySgmnParameters createParams(ImageDim dim) throws CreateException {
		return new BinarySgmnParameters(
			dim.getRes(),
			OptionalFactory.create(histogramProvider)
		);
	}
	
	private Optional<ObjectMask> mask(ImageDim dim) throws CreateException {
		Optional<BinaryChnl> maskChnl = ChnlProviderNullableCreator.createOptionalCheckSize(mask, "mask", dim);
		return maskChnl.map( chnl->
			new ObjectMask(chnl.binaryVoxelBox())
		);
	}

	public BinarySgmn getSgmn() {
		return sgmn;
	}

	public void setSgmn(BinarySgmn sgmn) {
		this.sgmn = sgmn;
	}

	public HistogramProvider getHistogramProvider() {
		return histogramProvider;
	}

	public void setHistogramProvider(HistogramProvider histogramProvider) {
		this.histogramProvider = histogramProvider;
	}

	public BinaryChnlProvider getMask() {
		return mask;
	}

	public void setMask(BinaryChnlProvider mask) {
		this.mask = mask;
	}
}
