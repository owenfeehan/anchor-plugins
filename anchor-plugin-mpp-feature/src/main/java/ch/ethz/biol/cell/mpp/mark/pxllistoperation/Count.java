package ch.ethz.biol.cell.mpp.mark.pxllistoperation;

import org.anchoranalysis.anchor.mpp.feature.bean.mark.PxlListOperationFromMark;
import org.anchoranalysis.anchor.mpp.pxlmark.memo.PxlMarkMemo;

/*
 * #%L
 * anchor-plugin-mpp-feature
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
import org.anchoranalysis.core.error.OperationFailedException;
import org.anchoranalysis.image.extent.ImageDim;

import ch.ethz.biol.cell.mpp.mark.pixelstatisticsfrommark.PixelStatisticsFromMark;

public class Count extends PxlListOperationFromMark {

	// START BEAN PROPERTIES
	@BeanField
	private PixelStatisticsFromMark pixelList;
	// END BEAN PROPERTIES
	
	@Override
	public double doOperation(PxlMarkMemo pxlMarkMemo, ImageDim dim) throws OperationFailedException {
		try {
			return pixelList.createStatisticsFor(pxlMarkMemo, dim).size();
		} catch (CreateException e) {
			throw new OperationFailedException(e);
		}
	}

	public PixelStatisticsFromMark getPixelList() {
		return pixelList;
	}

	public void setPixelList(PixelStatisticsFromMark pixelList) {
		this.pixelList = pixelList;
	}

}
