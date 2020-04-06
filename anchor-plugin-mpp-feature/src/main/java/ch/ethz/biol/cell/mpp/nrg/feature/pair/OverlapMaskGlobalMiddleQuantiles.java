package ch.ethz.biol.cell.mpp.nrg.feature.pair;

import org.anchoranalysis.anchor.mpp.feature.bean.nrg.elem.NRGElemPair;
import org.anchoranalysis.anchor.mpp.feature.nrg.elem.NRGElemPairCalcParams;
import org.anchoranalysis.anchor.mpp.mark.GlobalRegionIdentifiers;

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
import org.anchoranalysis.core.cache.ExecuteException;
import org.anchoranalysis.feature.cache.CacheableParams;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculationMaskGlobalMiddleQuantiles;

public class OverlapMaskGlobalMiddleQuantiles extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int regionID = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private int nrgIndex = 0;
	
	@BeanField
	private int maskValue = 255;
	
	@BeanField
	private double quantileLow = 0;
	
	@BeanField
	private double quantileHigh = 1;
	// END BEAN PROPERTIES
	
	public OverlapMaskGlobalMiddleQuantiles() {
	}
	
	@Override
	public double calcCast( CacheableParams<NRGElemPairCalcParams> params ) throws FeatureCalcException {
		
		try {
			return params.calc(
				new OverlapCalculationMaskGlobalMiddleQuantiles(
					regionID,
					nrgIndex,
					(byte) maskValue,
					quantileLow,
					quantileHigh
				)
			);
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}							
	}
	
	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public int getNrgIndex() {
		return nrgIndex;
	}

	public void setNrgIndex(int nrgIndex) {
		this.nrgIndex = nrgIndex;
	}

	public int getMaskValue() {
		return maskValue;
	}

	public void setMaskValue(int maskValue) {
		this.maskValue = maskValue;
	}

	public double getQuantileLow() {
		return quantileLow;
	}

	public void setQuantileLow(double quantileLow) {
		this.quantileLow = quantileLow;
	}

	public double getQuantileHigh() {
		return quantileHigh;
	}

	public void setQuantileHigh(double quantileHigh) {
		this.quantileHigh = quantileHigh;
	}

}
