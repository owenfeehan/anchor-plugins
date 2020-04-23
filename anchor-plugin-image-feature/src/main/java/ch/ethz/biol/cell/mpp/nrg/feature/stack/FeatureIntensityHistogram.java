package ch.ethz.biol.cell.mpp.nrg.feature.stack;

/*-
 * #%L
 * anchor-plugin-image-feature
 * %%
 * Copyright (C) 2010 - 2019 Owen Feehan, ETH Zurich, University of Zurich, Hoffmann la Roche
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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.cache.SessionInput;
import org.anchoranalysis.feature.cache.calculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.image.feature.bean.FeatureStack;
import org.anchoranalysis.image.feature.stack.FeatureInputStack;
import org.anchoranalysis.image.histogram.Histogram;

import ch.ethz.biol.cell.mpp.nrg.feature.stack.cachedcalculation.CalculateHistogram;
import ch.ethz.biol.cell.mpp.nrg.feature.stack.cachedcalculation.CalculateHistogramMasked;

/** Parent class for features that calculate a histogram from a particular channel and then extract
 *   a statistic
 */
public abstract class FeatureIntensityHistogram extends FeatureStack {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// START BEAN FIELDS
	@BeanField
	/** The channel that that forms the histogram */
	private int nrgIndex = 0;
	
	/** Optionally, index of another channel that masks the histogram. -1 disables */
	@BeanField
	private int nrgIndexMask = -1;
	// END BEAN FIELDS
	
	@Override
	public void beforeCalc()
			throws InitException {
		super.beforeCalc();
		
		if (nrgIndexMask < -1) {
			throw new InitException(
				String.format("nrgIndexMask must be either non-negative or -1 (off), but is: %d", nrgIndexMask)
			);
		}
	}
	
	@Override
	public double calc(SessionInput<FeatureInputStack> input) throws FeatureCalcException {
		Histogram h = input.calc(
			histogramCalculator()		
		);
		return calcStatistic(h);
	}
	
	protected abstract double calcStatistic( Histogram h );

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public int getNrgIndexMask() {
		return nrgIndexMask;
	}

	public void setNrgIndexMask(int nrgIndexMask) {
		this.nrgIndexMask = nrgIndexMask;
	}
	
	private CachedCalculation<Histogram,FeatureInputStack> histogramCalculator() {
		if (nrgIndexMask!=-1) {
			return new CalculateHistogramMasked(nrgIndex, nrgIndexMask);
		} else {
			return new CalculateHistogram(nrgIndex);
		}
	}
	
}
