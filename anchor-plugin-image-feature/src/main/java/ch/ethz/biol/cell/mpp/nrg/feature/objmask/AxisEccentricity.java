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


import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.math.moment.MomentsFromPointsCalculator;

/**
 * Calculates the eccentricity ala <a href="https://en.wikipedia.org/wiki/Image_moment">Image moment on Wikipedia</a>
 * 
 * @author Owen Feehan
 *
 */
public class AxisEccentricity extends AxisMomentsBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
	protected double calcFromMoments( MomentsFromPointsCalculator moments ) throws FeatureCalcException {
		double moments0 = moments.get(0).getEigenvalue();
		double moments1 = moments.get(1).getEigenvalue();
		
		if (moments0==0.0 && moments1==0.0) {
			return 1.0;
		}
		
		if (moments0==0.0 || moments1==0.0) {
			throw new FeatureCalcException("All moments are 0");
		}
		
		double eccentricity = calcEccentricity( moments1, moments0 );
		assert( !Double.isNaN(eccentricity) );
		return eccentricity;
	}
	
	public static double calcEccentricity( double eigenvalSmaller, double eigenvalLarger ) {
		return Math.sqrt( 1.0 - eigenvalSmaller/eigenvalLarger);
	}
}
