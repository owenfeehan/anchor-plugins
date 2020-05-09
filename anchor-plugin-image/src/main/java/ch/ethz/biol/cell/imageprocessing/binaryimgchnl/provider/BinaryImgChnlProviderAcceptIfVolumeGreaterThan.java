package ch.ethz.biol.cell.imageprocessing.binaryimgchnl.provider;

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
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProvider;
import org.anchoranalysis.image.bean.provider.BinaryImgChnlProviderOne;
import org.anchoranalysis.image.binary.BinaryChnl;

public class BinaryImgChnlProviderAcceptIfVolumeGreaterThan extends BinaryImgChnlProviderOne {

	// START BEAN PROPERTIES
	@BeanField
	private BinaryImgChnlProvider receiveProvider;
	
	@BeanField
	private double minRatio = 0.01;
	// END BEAN PROPERTIES

	@Override
	public BinaryChnl createFromChnl(BinaryChnl larger) throws CreateException {

		BinaryChnl smaller = receiveProvider.create();
		
		int countLarger = larger.countHighValues();
		int countSmaller = smaller.countHighValues();
		
		double ratio = ((double) countSmaller) / countLarger;
		
		if (ratio>minRatio) {
			return smaller;
		} else {
			return larger;
		}
	}

	public BinaryImgChnlProvider getReceiveProvider() {
		return receiveProvider;
	}

	public void setReceiveProvider(BinaryImgChnlProvider receiveProvider) {
		this.receiveProvider = receiveProvider;
	}

	public double getMinRatio() {
		return minRatio;
	}

	public void setMinRatio(double minRatio) {
		this.minRatio = minRatio;
	}


}
