package ch.ethz.biol.cell.mpp.nrg.feature.objmask;

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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.objmask.FeatureObjMask;
import org.anchoranalysis.image.feature.objmask.FeatureObjMaskParams;
import org.anchoranalysis.math.moment.MomentsFromPointsCalculator;
import org.anchoranalysis.points.moment.CalculateObjMaskSecondMomentMatrixMIP;

public class AxisLengthMIP extends FeatureObjMask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int index = 0;
	// END BEAN PROPERTIES
	
	@Override
	public double calc(CacheableParams<FeatureObjMaskParams> params) throws FeatureCalcException {
		
		return calcAxisLengthMIP(
			params,
			index
		);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	private double calcAxisLengthMIP( CacheableParams<FeatureObjMaskParams> params, int index ) throws FeatureCalcException {
		
		// THIS CAN BE DONE MORE EFFICIENTLY
		if (!params.getParams().getObjMask().hasPixelsGreaterThan(0)) {
			return Double.NaN;
		}
		// Justification
		// http://stackoverflow.com/questions/1711784/computing-object-statistics-from-the-second-central-moments
		// http://en.wikipedia.org/wiki/Image_moment
		try {
			MomentsFromPointsCalculator moments = params.calc(
				new CalculateObjMaskSecondMomentMatrixMIP()
			); 
			return moments.get( index ).eigenvalueNormalizedAsAxisLength();
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}
	}
}
