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
import org.anchoranalysis.image.binary.BinaryChnl;
import org.anchoranalysis.image.channel.Channel;
import org.anchoranalysis.image.object.factory.CreateFromEntireChnlFactory;

import lombok.Getter;
import lombok.Setter;

/** Assigns a scalar to the portion of the image covered by a mask */
public class ChnlProviderAssignScalar extends ChnlProviderOneMask {

	// START BEAN PROPERTIES
	@BeanField @Getter @Setter
	private double value;
	// END BEAN PROPERTIES
	
	@Override
	protected Channel createFromMaskedChnl(Channel chnl, BinaryChnl mask) {
		assignScalar(chnl, mask, (int) value);
		return chnl;
	}
	
	private void assignScalar(Channel chnlSrc, BinaryChnl mask, int value) {
		chnlSrc.getVoxelBox().any().setPixelsCheckMask(
			CreateFromEntireChnlFactory.createObject(mask),
			value
		);		
	}
}
