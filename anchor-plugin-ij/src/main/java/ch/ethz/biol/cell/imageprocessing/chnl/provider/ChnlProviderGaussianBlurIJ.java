package ch.ethz.biol.cell.imageprocessing.chnl.provider;

/*
 * #%L
 * anchor-plugin-ij
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


import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;

import org.anchoranalysis.bean.annotation.BeanField;
import org.anchoranalysis.bean.annotation.Positive;
import org.anchoranalysis.core.error.CreateException;
import org.anchoranalysis.image.bean.provider.ChnlProvider;
import org.anchoranalysis.image.chnl.Chnl;
import org.anchoranalysis.image.convert.IJWrap;
import org.anchoranalysis.image.voxel.box.VoxelBoxWrapper;

public class ChnlProviderGaussianBlurIJ extends ChnlProvider {

	// START BEAN PROPERTIES
	@BeanField
	private ChnlProvider chnlProvider;
	
	@BeanField @Positive
	private double sigma = 3;
	// END BEAN PROPERTIES

	@SuppressWarnings("deprecation")
	private Chnl blur( Chnl chnl ) {
		
		GaussianBlur gb = new GaussianBlur();

		VoxelBoxWrapper vb = chnl.getVoxelBox();
		
		// Are we missing a Z slice?
		for (int z=0; z<chnl.getDimensions().getZ(); z++) {
			ImageProcessor processor = IJWrap.imageProcessor(vb, z);
			gb.blur( processor, sigma );
		}
		
		return chnl;
	}
	
	@Override
	public Chnl create() throws CreateException {
		return blur( chnlProvider.create() );
	}

	public ChnlProvider getChnlProvider() {
		return chnlProvider;
	}

	public void setChnlProvider(ChnlProvider chnlProvider) {
		this.chnlProvider = chnlProvider;
	}

	public double getSigma() {
		return sigma;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

}
