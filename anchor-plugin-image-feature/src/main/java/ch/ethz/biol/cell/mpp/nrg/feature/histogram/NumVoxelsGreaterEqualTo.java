package ch.ethz.biol.cell.mpp.nrg.feature.histogram;

/*
 * #%L
 * anchor-plugin-image-feature
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
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.histogram.FeatureHistogram;
import org.anchoranalysis.image.feature.histogram.FeatureHistogramParams;
import org.anchoranalysis.image.histogram.Histogram;

public class NumVoxelsGreaterEqualTo extends FeatureHistogram {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int threshold = 0;
	// END BEAN PROPERTIES

	@Override
	public double calcCast(FeatureHistogramParams params)
			throws FeatureCalcException {
		
		Histogram h = params.getHistogram();
		
		double sum = 0.0;
		for( int i=threshold; i<=h.getMaxBin(); i++) {
			sum += h.getCount(i);
		}
		return sum;
	}

	// END BEAN PROPERTIES

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
}
