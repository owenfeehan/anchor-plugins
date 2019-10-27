package ch.ethz.biol.cell.mpp.nrg.feature.pair;

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
import org.anchoranalysis.core.error.InitException;
import org.anchoranalysis.feature.cache.CacheSession;
import org.anchoranalysis.feature.cachedcalculation.CachedCalculation;
import org.anchoranalysis.feature.calc.FeatureCalcException;
import org.anchoranalysis.feature.init.FeatureInitParams;

import ch.ethz.biol.cell.mpp.mark.GlobalRegionIdentifiers;
import ch.ethz.biol.cell.mpp.mark.pxlmark.memo.PxlMarkMemo;
import ch.ethz.biol.cell.mpp.nrg.NRGElemPairCalcParams;
import ch.ethz.biol.cell.mpp.nrg.cachedcalculation.OverlapCalculation;
import ch.ethz.biol.cell.mpp.nrg.NRGElemPair;

public class OverlapRatioNonModalValuesTwoRegions extends NRGElemPair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	// START BEAN PROPERTIES
	@BeanField
	private int regionID1 = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	
	@BeanField
	private int regionID2 = GlobalRegionIdentifiers.SUBMARK_INSIDE;
	// END BEAN PROPERTIES
	
	public OverlapRatioNonModalValuesTwoRegions() {
	}
	
	private CachedCalculation<Double> cc1;
	private CachedCalculation<Double> cc2;
	
	@Override
	public void beforeCalc(FeatureInitParams params, CacheSession cache)
			throws InitException {
		super.beforeCalc(params, cache);
		cc1 = cache.search( new OverlapCalculation(regionID1) );
		cc2 = cache.search( new OverlapCalculation(regionID2) );
		assert( cc1!=null );
		assert( cc2!=null );
	}
	
	private static double calcOverlapRatioMin( PxlMarkMemo obj1, PxlMarkMemo obj2, double overlap1, double overlap2, int regionID1, int regionID2 ) throws FeatureCalcException {

		double overlap = overlap1 + overlap2;
		
		if (overlap==0.0) {
			return 0.0;
		}
		
		double volume1 = OverlapRatio.calcMinVolume( obj1, obj2, regionID1 );
		double volume2 = OverlapRatio.calcMinVolume( obj1, obj2, regionID2 );
		return overlap / (volume1+volume2);
	}
	
	@Override
	public double calcCast( NRGElemPairCalcParams params ) throws FeatureCalcException {
		
		
		
		try {
			return calcOverlapRatioMin( params.getObj1(), params.getObj2(), cc1.getOrCalculate(params), cc2.getOrCalculate(params), regionID1, regionID2 );
		} catch (ExecuteException e) {
			throw new FeatureCalcException(e);
		}							
	}
	
	public int getRegionID1() {
		return regionID1;
	}

	public void setRegionID1(int regionID1) {
		this.regionID1 = regionID1;
	}

	public int getRegionID2() {
		return regionID2;
	}

	public void setRegionID2(int regionID2) {
		this.regionID2 = regionID2;
	}


}
